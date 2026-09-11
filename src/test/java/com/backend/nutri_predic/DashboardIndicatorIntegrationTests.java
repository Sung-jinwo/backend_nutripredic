package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.dashboard.service.DashboardService;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcc;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcs;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadTpp;
import com.backend.nutri_predic.indicador.dto.PccIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.PcsIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.TppIndicatorResponse;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import com.backend.nutri_predic.indicador.service.PcsIndicatorService;
import com.backend.nutri_predic.indicador.service.TppIndicatorService;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DashboardIndicatorIntegrationTests {
    @Test
    void dashboardComponeServiciosOficialesSinRecalcularIndicadores() {
        ClienteRepository clientes = mock(ClienteRepository.class);
        PrediccionModeloRepository predicciones = mock(PrediccionModeloRepository.class);
        PccIndicatorService pcc = mock(PccIndicatorService.class);
        PcsIndicatorService pcs = mock(PcsIndicatorService.class);
        TppIndicatorService tpp = mock(TppIndicatorService.class);
        when(pcc.obtener())
                .thenReturn(
                        new PccIndicatorResponse(
                                50.0, 2, 1, EstadoDisponibilidadPcc.DISPONIBLE, null));
        when(pcs.obtener())
                .thenReturn(
                        new PcsIndicatorResponse(
                                null, 0, 0, EstadoDisponibilidadPcs.NO_DISPONIBLE, "SIN_PCS"));
        when(tpp.obtener())
                .thenReturn(
                        new TppIndicatorResponse(
                                null,
                                0,
                                0,
                                EstadoDisponibilidadTpp.NO_DISPONIBLE,
                                "SIN_TPP",
                                Map.of()));
        var modelo = mock(PrediccionModelo.class);
        when(modelo.getModelVersion()).thenReturn("v6-test");
        when(modelo.getSchemaVersion()).thenReturn("variables-modelo-v6");
        when(predicciones.findFirstByOrderByFechaPrediccionDesc())
                .thenReturn(Optional.of(modelo));
        DashboardService dashboard = new DashboardService(clientes, predicciones, pcc, pcs, tpp);

        var resultado = dashboard.dashboard();

        assertThat(resultado.pcc().porcentajePcc()).isEqualTo(50.0);
        assertThat(resultado.porcentajeNivelConsumoOperativo()).isNull();
        assertThat(resultado.modeloActivo().version()).isEqualTo("v6-test");
        assertThat(resultado.pcs().porcentajePcs()).isNull();
        assertThat(resultado.tpp().promedioTppMs()).isNull();
    }
}
