package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.auth.dto.RegisterRequest;
import com.backend.nutri_predic.auth.service.AuthService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.estudio.entity.*;
import com.backend.nutri_predic.estudio.repository.EstudioRepository;
import com.backend.nutri_predic.estudio.repository.ParticipacionEstudioRepository;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.dto.PrediccionModeloHistorialResponse;
import com.backend.nutri_predic.prediccionmodelo.dto.PrediccionModeloResponse;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class PrediccionModeloPersistenceIntegrationTests {
    @Autowired AuthService auth;
    @Autowired ClienteRepository clientes;
    @Autowired EstudioRepository estudios;
    @Autowired ParticipacionEstudioRepository participaciones;
    @Autowired PrediccionModeloRepository predicciones;

    @Test
    @Transactional
    void persistePrediccionConClienteSinParticipacionYConTresProbabilidades() {
        var cliente = cliente("sin-participacion");
        var prediccion =
                predicciones.saveAndFlush(
                        exitosa(
                                cliente.getId(),
                                null,
                                MomentoEvaluacion.NO_DETERMINADO,
                                Instant.parse("2026-08-20T10:00:00Z")));

        var recuperada = predicciones.findById(prediccion.getId()).orElseThrow();
        assertThat(recuperada.getCliente().getId()).isEqualTo(cliente.getId());
        assertThat(recuperada.getParticipacionEstudio()).isNull();
        assertThat(recuperada.getMomentoEvaluacion()).isEqualTo(MomentoEvaluacion.NO_DETERMINADO);
        assertThat(recuperada.getProbAdecuado()).isEqualByComparingTo("0.200000");
        assertThat(recuperada.getProbMejorable()).isEqualByComparingTo("0.650000");
        assertThat(recuperada.getProbCritico()).isEqualByComparingTo("0.150000");
        assertThat(PrediccionModeloResponse.from(recuperada).schemaVersion())
                .isEqualTo("variables-modelo-v4");
    }

    @Test
    @Transactional
    void persisteParticipacionMomentosHistorialEIndiceCandidato() {
        var cliente = cliente("historial");
        var participacion = participacion(cliente.getId());
        var basal =
                predicciones.saveAndFlush(
                        exitosa(
                                cliente.getId(),
                                participacion.getId(),
                                MomentoEvaluacion.BASAL,
                                Instant.parse("2026-08-20T08:00:00Z")));
        predicciones.saveAndFlush(
                exitosa(
                        cliente.getId(),
                        participacion.getId(),
                        MomentoEvaluacion.FINAL,
                        Instant.parse("2026-08-20T09:00:00Z")));
        predicciones.saveAndFlush(
                exitosa(
                        cliente.getId(),
                        null,
                        MomentoEvaluacion.NO_DETERMINADO,
                        Instant.parse("2026-08-20T10:00:00Z")));

        var historial = predicciones.findByClienteIdOrderByFechaPrediccionDesc(cliente.getId());
        assertThat(historial)
                .extracting(PrediccionModelo::getMomentoEvaluacion)
                .containsExactly(
                        MomentoEvaluacion.NO_DETERMINADO,
                        MomentoEvaluacion.FINAL,
                        MomentoEvaluacion.BASAL);
        assertThat(historial.getLast().getParticipacionEstudio().getId())
                .isEqualTo(participacion.getId());
        assertThat(
                        predicciones
                                .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndModelVersionAndEstadoOrderByFechaPrediccionDesc(
                                        cliente.getId(),
                                        LocalDate.of(2026, 8, 20),
                                        MomentoEvaluacion.BASAL,
                                        "modelo-prueba-v1",
                                        EstadoPrediccionModelo.EXITOSA))
                .contains(basal);
        assertThat(PrediccionModeloHistorialResponse.from(historial.getFirst()).estado())
                .isEqualTo("EXITOSA");
    }

    private PrediccionModelo exitosa(
            Long clienteId, Long participacionId, MomentoEvaluacion momento, Instant fecha) {
        var prediccion = new PrediccionModelo();
        prediccion.setCliente(clientes.findById(clienteId).orElseThrow());
        if (participacionId != null)
            prediccion.setParticipacionEstudio(
                    participaciones.findById(participacionId).orElseThrow());
        prediccion.setMomentoEvaluacion(momento);
        prediccion.setFechaCorte(LocalDate.of(2026, 8, 20));
        prediccion.setClasificacionPredicha(ClasificacionPerfilHabitos.MEJORABLE);
        prediccion.setProbAdecuado(new BigDecimal("0.200000"));
        prediccion.setProbMejorable(new BigDecimal("0.650000"));
        prediccion.setProbCritico(new BigDecimal("0.150000"));
        prediccion.setModelVersion("modelo-prueba-v1");
        prediccion.setSchemaVersion("variables-modelo-v4");
        prediccion.setFechaPrediccion(fecha);
        prediccion.setTiempoInferenciaMs(42L);
        prediccion.setEstado(EstadoPrediccionModelo.EXITOSA);
        return prediccion;
    }

    private com.backend.nutri_predic.cliente.entity.Cliente cliente(String prefijo) {
        var registro =
                auth.register(
                        new RegisterRequest(
                                prefijo + "-" + UUID.randomUUID() + "@test.local",
                                "Password1!",
                                "Cliente"));
        return clientes.findById(registro.clienteId()).orElseThrow();
    }

    private ParticipacionEstudio participacion(Long clienteId) {
        var estudio = new Estudio();
        estudio.setCodigo("EST-" + UUID.randomUUID());
        estudio.setNombre("Estudio de prueba");
        estudio.setEstado(EstadoEstudio.ACTIVO);
        estudio = estudios.save(estudio);
        var participacion = new ParticipacionEstudio();
        participacion.setEstudio(estudio);
        participacion.setCliente(clientes.findById(clienteId).orElseThrow());
        participacion.setCodigoParticipante("P-" + UUID.randomUUID());
        participacion.setGrupo(GrupoEstudio.EXPERIMENTAL);
        participacion.setEstado(EstadoEstudio.ACTIVO);
        participacion.setFechaAsignacion(LocalDate.of(2026, 8, 1));
        return participaciones.save(participacion);
    }
}
