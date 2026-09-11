package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcc;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import com.backend.nutri_predic.conocimiento.evaluacion.entity.ResultadoTest;
import com.backend.nutri_predic.conocimiento.evaluacion.repository.ResultadoTestRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PccIndicatorServiceTests {
    private final ResultadoTestRepository resultados = mock(ResultadoTestRepository.class);
    private final PccIndicatorService pcc = new PccIndicatorService(resultados);

    @Test
    void sinResultadosValidosNoConvierteAusenciaEnCero() {
        when(resultados.findByEstadoValidezAndNivelIsNotNull(EstadoValidezMedicion.VALIDA))
                .thenReturn(List.of());

        var resultado = pcc.obtener();

        assertThat(resultado.porcentajePcc()).isNull();
        assertThat(resultado.estadoDisponibilidad())
                .isEqualTo(EstadoDisponibilidadPcc.NO_DISPONIBLE);
        assertThat(resultado.motivoNoDisponible())
                .isEqualTo(PccIndicatorService.SIN_RESULTADOS_VALIDOS);
    }

    @Test
    void usaUltimoResultadoTestValidoPorCliente() {
        ResultadoTest anteriorBajo =
                resultado(1L, 10L, "2026-08-01T10:00:00Z", NivelConocimiento.BAJO);
        ResultadoTest ultimoAlto =
                resultado(1L, 11L, "2026-08-02T10:00:00Z", NivelConocimiento.ALTO);
        ResultadoTest otroBajo = resultado(2L, 12L, "2026-08-01T11:00:00Z", NivelConocimiento.BAJO);
        when(resultados.findByEstadoValidezAndNivelIsNotNull(EstadoValidezMedicion.VALIDA))
                .thenReturn(List.of(anteriorBajo, otroBajo, ultimoAlto));

        var resultado = pcc.obtener();

        assertThat(resultado.porcentajePcc()).isEqualTo(50.0);
        assertThat(resultado.totalEvaluadosValidos()).isEqualTo(2);
        assertThat(resultado.totalBajoConocimiento()).isEqualTo(1);
        assertThat(resultado.estadoDisponibilidad()).isEqualTo(EstadoDisponibilidadPcc.DISPONIBLE);
    }

    private ResultadoTest resultado(
            Long clienteId, Long id, String fecha, NivelConocimiento nivel) {
        Cliente cliente = mock(Cliente.class);
        when(cliente.getId()).thenReturn(clienteId);
        ResultadoTest resultado = mock(ResultadoTest.class);
        when(resultado.getCliente()).thenReturn(cliente);
        when(resultado.getId()).thenReturn(id);
        when(resultado.getFecha()).thenReturn(Instant.parse(fecha));
        when(resultado.getNivel()).thenReturn(nivel);
        return resultado;
    }
}
