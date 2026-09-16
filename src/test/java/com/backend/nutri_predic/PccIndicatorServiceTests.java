package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcc;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class PccIndicatorServiceTests {
    private final SesionConocimientoIaRepository sesiones = mock(SesionConocimientoIaRepository.class);
    private final PccIndicatorService pcc = new PccIndicatorService(sesiones);

    @Test
    void sinSesionesDiariasValidasNoConvierteAusenciaEnCero() {
        when(sesiones.findByEstadoAndEstadoValidez(
                EstadoSesionConocimientoIa.RESPONDIDA, EstadoValidezMedicion.VALIDA))
                .thenReturn(List.of());

        var resultado = pcc.obtener();

        assertThat(resultado.porcentajePcc()).isNull();
        assertThat(resultado.estadoDisponibilidad()).isEqualTo(EstadoDisponibilidadPcc.NO_DISPONIBLE);
        assertThat(resultado.motivoNoDisponible()).isEqualTo(PccIndicatorService.SIN_RESULTADOS_VALIDOS);
    }

    @Test
    void usaLaUltimaEvaluacionAdaptativaValidaPorCliente() {
        var anteriorBajo = sesion(1L, 10L, "2026-08-01", "2026-08-01T10:00:00Z", "BAJO");
        var ultimaAlta = sesion(1L, 11L, "2026-08-02", "2026-08-02T10:00:00Z", "ALTO");
        var otroBajo = sesion(2L, 12L, "2026-08-01", "2026-08-01T11:00:00Z", "BAJO");
        when(sesiones.findByEstadoAndEstadoValidez(
                EstadoSesionConocimientoIa.RESPONDIDA, EstadoValidezMedicion.VALIDA))
                .thenReturn(List.of(anteriorBajo, otroBajo, ultimaAlta));

        var resultado = pcc.obtener();

        assertThat(resultado.porcentajePcc()).isEqualTo(50.0);
        assertThat(resultado.totalEvaluadosValidos()).isEqualTo(2);
        assertThat(resultado.totalBajoConocimiento()).isEqualTo(1);
        assertThat(resultado.estadoDisponibilidad()).isEqualTo(EstadoDisponibilidadPcc.DISPONIBLE);
    }

    private SesionConocimientoIa sesion(
            Long clienteId, Long id, String fecha, String respondidaEn, String nivel) {
        var sesion = mock(SesionConocimientoIa.class);
        when(sesion.getClienteId()).thenReturn(clienteId);
        when(sesion.getId()).thenReturn(id);
        when(sesion.getFechaEvaluacion()).thenReturn(LocalDate.parse(fecha));
        when(sesion.getRespondidaEn()).thenReturn(Instant.parse(respondidaEn));
        when(sesion.getNivelResultado()).thenReturn(nivel);
        return sesion;
    }
}
