package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.PreguntaGeneradaIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.PreguntaGeneradaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PreguntaGeneradaIaPersistenceTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired PrediccionModeloRepository predicciones;
    @Autowired SesionConocimientoIaRepository sesiones;
    @Autowired PreguntaGeneradaIaRepository preguntas;
    @Autowired EntityManager entityManager;

    @Test
    @Transactional
    void persisteOpcionesSnakeCaseYRelacionDeRetry() {
        var registro =
                auth.register(
                        new RegisterRequest(
                                "persistencia-ia-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Persistencia IA"));
        var cliente = clientes.findById(registro.clienteId()).orElseThrow();

        var prediccion = new PrediccionModelo();
        prediccion.setCliente(cliente);
        prediccion.setFechaCorte(LocalDate.now());
        prediccion.setMomentoEvaluacion(MomentoEvaluacion.NO_DETERMINADO);
        prediccion.setSchemaVersion("variables-modelo-v5");
        prediccion.setModelVersion("persistencia-pcc-ia-test");
        prediccion.setClasificacionPredicha(ClasificacionPerfilHabitos.MEJORABLE);
        prediccion.setProbAdecuado(new BigDecimal("0.200000"));
        prediccion.setProbMejorable(new BigDecimal("0.600000"));
        prediccion.setProbCritico(new BigDecimal("0.200000"));
        prediccion.setEstado(EstadoPrediccionModelo.EXITOSA);
        prediccion = predicciones.save(prediccion);

        var origen =
                sesion(
                        prediccion,
                        "persistencia-origen",
                        EstadoSesionConocimientoIa.IA_NO_DISPONIBLE);
        origen = sesiones.save(origen);
        var retry = sesion(prediccion, "persistencia-retry", EstadoSesionConocimientoIa.GENERADA);
        retry.setReintentoDeSesion(origen);
        retry = sesiones.save(retry);

        var pregunta = new PreguntaGeneradaIa();
        pregunta.setSesion(retry);
        pregunta.setTema("PROTEINA");
        pregunta.setSubtema("General");
        pregunta.setDificultad("MEDIA");
        pregunta.setEnunciado("Pregunta técnica de persistencia");
        pregunta.setOpcionA("Opción A");
        pregunta.setOpcionB("Opción B");
        pregunta.setOpcionC("Opción C");
        pregunta.setOpcionD("Opción D");
        pregunta.setRespuestaCorrecta("B");
        pregunta.setExplicacion("Explicación técnica");
        pregunta.setOrden(1);
        pregunta = preguntas.saveAndFlush(pregunta);
        entityManager.clear();

        var recuperada = preguntas.findById(pregunta.getId()).orElseThrow();
        assertThat(recuperada.getOpcionA()).isEqualTo("Opción A");
        assertThat(recuperada.getOpcionB()).isEqualTo("Opción B");
        assertThat(recuperada.getOpcionC()).isEqualTo("Opción C");
        assertThat(recuperada.getOpcionD()).isEqualTo("Opción D");
        assertThat(recuperada.getSesion().getReintentoDeSesion().getId()).isEqualTo(origen.getId());

        var columnasFisicas =
                (Object[])
                        entityManager
                                .createNativeQuery(
                                        "select opcion_a, opcion_b, opcion_c, opcion_d from preguntas_generadas_ia where id = :id")
                                .setParameter("id", pregunta.getId())
                                .getSingleResult();
        assertThat(columnasFisicas).containsExactly("Opción A", "Opción B", "Opción C", "Opción D");
    }

    private SesionConocimientoIa sesion(
            PrediccionModelo prediccion, String configuracion, EstadoSesionConocimientoIa estado) {
        var sesion = new SesionConocimientoIa();
        sesion.setPrediccionModelo(prediccion);
        sesion.setConfiguracionVersion(configuracion);
        sesion.setModelVersionPredictivo(prediccion.getModelVersion());
        sesion.setSchemaVersion(prediccion.getSchemaVersion());
        sesion.setProveedorIa("GEMINI");
        sesion.setModeloGenerativo("gemini-test");
        sesion.setEstado(estado);
        return sesion;
    }
}
