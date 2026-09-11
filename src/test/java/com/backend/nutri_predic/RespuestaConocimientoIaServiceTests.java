package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest.Respuesta;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.PreguntaGeneradaIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.PreguntaGeneradaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.RespuestaAdaptativaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.conocimiento.practica.service.RespuestaConocimientoIaService;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class RespuestaConocimientoIaServiceTests {
    private final SesionConocimientoIaRepository sesiones =
            mock(SesionConocimientoIaRepository.class);
    private final PreguntaGeneradaIaRepository preguntas = mock(PreguntaGeneradaIaRepository.class);
    private final RespuestaAdaptativaIaRepository respuestas =
            mock(RespuestaAdaptativaIaRepository.class);
    private final AccessService access = mock(AccessService.class);
    private final Authentication authentication = mock(Authentication.class);
    private final RespuestaConocimientoIaService service =
            new RespuestaConocimientoIaService(sesiones, preguntas, respuestas, access);

    private SesionConocimientoIa sesion;
    private PreguntaGeneradaIa primera;
    private PreguntaGeneradaIa segunda;

    @BeforeEach
    void setUp() {
        Cliente cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(1L);
        PrediccionModelo prediccion = mock(PrediccionModelo.class);
        when(prediccion.getCliente()).thenReturn(cliente);
        sesion = new SesionConocimientoIa();
        ReflectionTestUtils.setField(sesion, "id", 10L);
        sesion.setPrediccionModelo(prediccion);
        sesion.setEstado(EstadoSesionConocimientoIa.GENERADA);
        primera = pregunta(101L, "B", "Explicación uno");
        segunda = pregunta(102L, "C", "Explicación dos");
        when(sesiones.findByIdForUpdate(10L)).thenReturn(Optional.of(sesion));
        when(preguntas.findBySesionIdOrderByOrdenAsc(10L)).thenReturn(List.of(primera, segunda));
        when(respuestas.existsBySesionId(10L)).thenReturn(false);
        when(respuestas.saveAll(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(sesiones.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void entregaValidaPersisteYCalculaCorrectoEIncorrectoEnBackend() {
        var request =
                new ResponderConocimientoIaRequest(
                        List.of(new Respuesta(101L, "B"), new Respuesta(102L, "A")));

        var resultado = service.responder(1L, 10L, request, authentication);

        assertThat(resultado.totalPreguntas()).isEqualTo(2);
        assertThat(resultado.totalRespondidas()).isEqualTo(2);
        assertThat(resultado.correctas()).isEqualTo(1);
        assertThat(resultado.porcentajeAdaptativo()).isEqualTo(50.0);
        assertThat(resultado.estado()).isEqualTo("RESPONDIDA");
        assertThat(resultado.respuestas().getFirst().respuestaCorrecta()).isEqualTo("B");
        assertThat(resultado.respuestas().getFirst().explicacion()).isEqualTo("Explicación uno");
        assertThat(sesion.getEstado()).isEqualTo(EstadoSesionConocimientoIa.RESPONDIDA);
        verify(respuestas).saveAll(any());
    }

    @Test
    void preguntaAjenaEsRechazada() {
        assertThatThrownBy(
                        () ->
                                service.responder(
                                        1L,
                                        10L,
                                        new ResponderConocimientoIaRequest(
                                                List.of(
                                                        new Respuesta(101L, "A"),
                                                        new Respuesta(999L, "B"))),
                                        authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ajenas");
        verify(respuestas, never()).saveAll(any());
    }

    @Test
    void preguntaDuplicadaEsRechazada() {
        assertThatThrownBy(
                        () ->
                                service.responder(
                                        1L,
                                        10L,
                                        new ResponderConocimientoIaRequest(
                                                List.of(
                                                        new Respuesta(101L, "A"),
                                                        new Respuesta(101L, "B"))),
                                        authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("duplicadas");
    }

    @Test
    void conjuntoIncompletoEsRechazado() {
        assertThatThrownBy(
                        () ->
                                service.responder(
                                        1L,
                                        10L,
                                        new ResponderConocimientoIaRequest(
                                                List.of(new Respuesta(101L, "A"))),
                                        authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exactamente");
    }

    @Test
    void segundaEntregaEsRechazadaSinSobrescribir() {
        sesion.marcarRespondida(Instant.now());

        assertThatThrownBy(
                        () ->
                                service.responder(
                                        1L,
                                        10L,
                                        new ResponderConocimientoIaRequest(
                                                List.of(
                                                        new Respuesta(101L, "B"),
                                                        new Respuesta(102L, "C"))),
                                        authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue respondida");
        verify(respuestas, never()).saveAll(any());
    }

    @Test
    void opcionQueNoExisteEsRechazada() {
        assertThatThrownBy(
                        () ->
                                service.responder(
                                        1L,
                                        10L,
                                        new ResponderConocimientoIaRequest(
                                                List.of(
                                                        new Respuesta(101L, "Z"),
                                                        new Respuesta(102L, "C"))),
                                        authentication))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("opción seleccionada no existe");
    }

    private PreguntaGeneradaIa pregunta(Long id, String correcta, String explicacion) {
        PreguntaGeneradaIa pregunta = new PreguntaGeneradaIa();
        ReflectionTestUtils.setField(pregunta, "id", id);
        pregunta.setSesion(sesion);
        pregunta.setOpcionA("Opción A");
        pregunta.setOpcionB("Opción B");
        pregunta.setOpcionC("Opción C");
        pregunta.setOpcionD("Opción D");
        pregunta.setRespuestaCorrecta(correcta);
        pregunta.setExplicacion(explicacion);
        return pregunta;
    }
}
