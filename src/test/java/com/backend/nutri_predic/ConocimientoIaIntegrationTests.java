package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.conocimiento.practica.dto.GenerarConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest.Respuesta;
import com.backend.nutri_predic.conocimiento.practica.repository.*;
import com.backend.nutri_predic.conocimiento.gemini.repository.TrazaLlamadaGeminiRepository;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import com.backend.nutri_predic.conocimiento.practica.service.RespuestaConocimientoIaService;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.entity.*;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.ResultadoTestRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class ConocimientoIaIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired PrediccionModeloRepository predicciones;
    @Autowired GeneracionPreguntasConocimientoService service;
    @Autowired RespuestaConocimientoIaService respuestasService;
    @Autowired PreguntaGeneradaIaRepository preguntas;
    @Autowired RespuestaAdaptativaIaRepository respuestas;
    @Autowired SesionConocimientoIaRepository sesiones;
    @Autowired TrazaLlamadaGeminiRepository trazas;
    @Autowired ResultadoTestRepository resultadosTest;
    @Autowired PccIndicatorService pcc;

    @Test
    void respuestaDeSesionDiariaV6ActualizaPccInmediatamente() {
        var admin = admin();
        var registro = auth.register(new RegisterRequest(
                "pcc-diario-" + UUID.randomUUID() + "@test.local", "Password1!", "PCC Diario"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();
        var prediccion = new PrediccionModelo();
        prediccion.setCliente(cliente);
        prediccion.setFechaCorte(LocalDate.now());
        prediccion.setMomentoEvaluacion(MomentoEvaluacion.DIARIO);
        prediccion.setSchemaVersion("variables-modelo-v6");
        prediccion.setModelVersion(ModeloPredictivoV6Service.MODEL_VERSION_ESPERADA);
        prediccion.setClasificacionPredicha(ClasificacionPerfilHabitos.MEJORABLE);
        prediccion.setProbAdecuado(new BigDecimal("0.2"));
        prediccion.setProbMejorable(new BigDecimal("0.6"));
        prediccion.setProbCritico(new BigDecimal("0.2"));
        prediccion.setEstado(EstadoPrediccionModelo.EXITOSA);
        prediccion = predicciones.save(prediccion);

        var generada = service.generarAutomatico(prediccion.getId());
        var preguntasDiarias = preguntas.findBySesionIdOrderByOrdenAsc(generada.sesionId());
        assertThat(preguntasDiarias).hasSize(5);
        respuestasService.responder(
                cliente.getId(), generada.sesionId(),
                new ResponderConocimientoIaRequest(preguntasDiarias.stream()
                        .map(q -> new Respuesta(q.getId(), opcionIncorrecta(q.getRespuestaCorrecta())))
                        .toList()), admin);

        var indicador = pcc.obtener();
        assertThat(indicador.totalEvaluadosValidos()).isEqualTo(1);
        assertThat(indicador.totalBajoConocimiento()).isEqualTo(1);
        assertThat(indicador.porcentajePcc()).isEqualTo(100.0);
    }

    @Test
    @Transactional
    void mockGeneraBloqueUnaVezYNoExponeSoluciones() {
        var registro =
                auth.register(
                        new RegisterRequest(
                                "ia-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "IA Test"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();
        var p = new PrediccionModelo();
        p.setCliente(cliente);
        p.setFechaCorte(LocalDate.now());
        p.setMomentoEvaluacion(MomentoEvaluacion.NO_DETERMINADO);
        p.setSchemaVersion("variables-modelo-v5");
        p.setModelVersion("rf-v5-test");
        p.setClasificacionPredicha(ClasificacionPerfilHabitos.MEJORABLE);
        p.setProbAdecuado(new BigDecimal("0.2"));
        p.setProbMejorable(new BigDecimal("0.6"));
        p.setProbCritico(new BigDecimal("0.2"));
        p.setEstado(EstadoPrediccionModelo.EXITOSA);
        p = predicciones.save(p);
        var admin =
                new UsernamePasswordAuthenticationToken(
                        "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var request = new GenerarConocimientoIaRequest(List.of("PROTEINA"), "MEDIA", 2);
        var primera = service.generar(cliente.getId(), request, admin);
        var segunda = service.generar(cliente.getId(), request, admin);
        var get = service.obtener(cliente.getId(), admin);
        assertThat(primera.preguntasAdaptativas()).hasSize(2);
        assertThat(primera.sesionId()).isEqualTo(segunda.sesionId()).isEqualTo(get.sesionId());
        assertThat(preguntas.findBySesionIdOrderByOrdenAsc(primera.sesionId())).hasSize(2);
        assertThat(trazas.count()).isEqualTo(2);
        assertThat(primera.preguntasAdaptativas().getFirst().toString())
                .doesNotContain("respuestaCorrecta")
                .doesNotContain("Explicación");
        assertThat(primera.resultadoAdaptativo()).isNull();
    }

    @Test
    @Transactional
    void respuestasCorrectasEIncorrectasPersistenSinModificarResultadoTestNiPcc() {
        var admin = admin();
        long totalResultadosAntes = resultadosTest.count();
        var pccAntes = pcc.obtener();

        var correcta = generarSesion("correcta", admin);
        var preguntasCorrectas = preguntas.findBySesionIdOrderByOrdenAsc(correcta.sesionId());
        var resultadoCorrecto =
                respuestasService.responder(
                        correcta.clienteId(),
                        correcta.sesionId(),
                        new ResponderConocimientoIaRequest(
                                preguntasCorrectas.stream()
                                        .map(
                                                pregunta ->
                                                        new Respuesta(
                                                                pregunta.getId(),
                                                                pregunta.getRespuestaCorrecta()))
                                        .toList()),
                        admin);
        assertThat(resultadoCorrecto.correctas()).isEqualTo(2);
        assertThat(resultadoCorrecto.porcentajeAdaptativo()).isEqualTo(100.0);

        var incorrecta = generarSesion("incorrecta", admin);
        var preguntasIncorrectas = preguntas.findBySesionIdOrderByOrdenAsc(incorrecta.sesionId());
        var resultadoIncorrecto =
                respuestasService.responder(
                        incorrecta.clienteId(),
                        incorrecta.sesionId(),
                        new ResponderConocimientoIaRequest(
                                preguntasIncorrectas.stream()
                                        .map(
                                                pregunta ->
                                                        new Respuesta(
                                                                pregunta.getId(),
                                                                opcionIncorrecta(
                                                                        pregunta
                                                                                .getRespuestaCorrecta())))
                                        .toList()),
                        admin);
        assertThat(resultadoIncorrecto.correctas()).isZero();
        assertThat(resultadoIncorrecto.porcentajeAdaptativo()).isZero();

        assertThat(respuestas.count()).isEqualTo(4);
        assertThat(sesiones.findById(correcta.sesionId()).orElseThrow().getEstado().name())
                .isEqualTo("RESPONDIDA");
        var consulta = service.obtener(correcta.clienteId(), admin);
        assertThat(consulta.estadoAdaptativo()).isEqualTo("RESPONDIDA");
        assertThat(consulta.resultadoAdaptativo()).isNotNull();
        assertThat(consulta.resultadoAdaptativo().respuestas())
                .allSatisfy(
                        respuesta -> {
                            assertThat(respuesta.respuestaCorrecta()).isNotBlank();
                            assertThat(respuesta.explicacion()).isNotBlank();
                        });

        assertThat(resultadosTest.count()).isEqualTo(totalResultadosAntes);
        assertThat(pcc.obtener()).isEqualTo(pccAntes);
    }

    private SesionGenerada generarSesion(String sufijo, Authentication admin) {
        var registro =
                auth.register(
                        new RegisterRequest(
                                "ia-respuestas-" + sufijo + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "IA Respuestas"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();
        var prediccion = new PrediccionModelo();
        prediccion.setCliente(cliente);
        prediccion.setFechaCorte(LocalDate.now());
        prediccion.setMomentoEvaluacion(MomentoEvaluacion.NO_DETERMINADO);
        prediccion.setSchemaVersion("variables-modelo-v5");
        prediccion.setModelVersion("rf-v5-test");
        prediccion.setClasificacionPredicha(ClasificacionPerfilHabitos.MEJORABLE);
        prediccion.setProbAdecuado(new BigDecimal("0.2"));
        prediccion.setProbMejorable(new BigDecimal("0.6"));
        prediccion.setProbCritico(new BigDecimal("0.2"));
        prediccion.setEstado(EstadoPrediccionModelo.EXITOSA);
        predicciones.save(prediccion);
        var sesion =
                service.generar(
                        cliente.getId(),
                        new GenerarConocimientoIaRequest(List.of("PROTEINA"), "MEDIA", 2),
                        admin);
        return new SesionGenerada(cliente.getId(), sesion.sesionId());
    }

    private Authentication admin() {
        return new UsernamePasswordAuthenticationToken(
                "admin@test.local", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private String opcionIncorrecta(String correcta) {
        return "A".equalsIgnoreCase(correcta) ? "B" : "A";
    }

    private record SesionGenerada(Long clienteId, Long sesionId) {}
}
