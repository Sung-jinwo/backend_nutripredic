package com.backend.nutri_predic;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.dashboard.controller.DashboardController;
import com.backend.nutri_predic.dashboard.dto.DashboardResponse;
import com.backend.nutri_predic.dashboard.service.DashboardService;
import com.backend.nutri_predic.indicador.controller.IndicadorOficialController;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcc;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadPcs;
import com.backend.nutri_predic.indicador.dto.EstadoDisponibilidadTpp;
import com.backend.nutri_predic.indicador.dto.PccIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.PcsIndicatorResponse;
import com.backend.nutri_predic.indicador.dto.TppIndicatorResponse;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import com.backend.nutri_predic.indicador.service.PcsIndicatorService;
import com.backend.nutri_predic.indicador.service.TppIndicatorService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class IndicatorHttpContractTests {
    private final PccIndicatorService pcc = mock(PccIndicatorService.class);
    private final PcsIndicatorService pcs = mock(PcsIndicatorService.class);
    private final TppIndicatorService tpp = mock(TppIndicatorService.class);
    private final MockMvc indicadores =
            MockMvcBuilders.standaloneSetup(new IndicadorOficialController(pcc, pcs, tpp)).build();

    @Test
    void indicadoresNoDisponiblesConservanNullYMotivo() throws Exception {
        when(pcc.obtener())
                .thenReturn(
                        new PccIndicatorResponse(
                                null,
                                0,
                                0,
                                EstadoDisponibilidadPcc.NO_DISPONIBLE,
                                "SIN_RESULTADOS_VALIDOS"));
        when(pcs.obtener())
                .thenReturn(
                        new PcsIndicatorResponse(
                                null,
                                0,
                                0,
                                EstadoDisponibilidadPcs.NO_DISPONIBLE,
                                "CLASIFICACION_METODOLOGICA_NO_DISPONIBLE"));
        when(tpp.obtener())
                .thenReturn(
                        new TppIndicatorResponse(
                                null,
                                0,
                                0,
                                EstadoDisponibilidadTpp.NO_DISPONIBLE,
                                "SIN_ANALISIS_VALIDOS",
                                Map.of()));

        indicadores
                .perform(get("/api/admin/indicadores/pcc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajePcc").value(nullValue()))
                .andExpect(jsonPath("$.totalEvaluadosValidos").value(0))
                .andExpect(jsonPath("$.motivoNoDisponible").value("SIN_RESULTADOS_VALIDOS"));
        indicadores
                .perform(get("/api/admin/indicadores/pcs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentajePcs").value(nullValue()))
                .andExpect(
                        jsonPath("$.motivoNoDisponible")
                                .value("CLASIFICACION_METODOLOGICA_NO_DISPONIBLE"));
        indicadores
                .perform(get("/api/admin/indicadores/tpp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedioTppMs").value(nullValue()))
                .andExpect(jsonPath("$.motivoNoDisponible").value("SIN_ANALISIS_VALIDOS"));
    }

    @Test
    void dashboardAnidaIndicadoresYSuprimeCamposLegacyAmbiguos() throws Exception {
        DashboardService service = mock(DashboardService.class);
        when(service.dashboard())
                .thenReturn(
                        new DashboardResponse(
                                10,
                                8,
                                new PccIndicatorResponse(
                                        50.0, 2, 1, EstadoDisponibilidadPcc.DISPONIBLE, null),
                                new PcsIndicatorResponse(
                                        null,
                                        0,
                                        0,
                                        EstadoDisponibilidadPcs.NO_DISPONIBLE,
                                        "CLASIFICACION_METODOLOGICA_NO_DISPONIBLE"),
                                new TppIndicatorResponse(
                                        null,
                                        0,
                                        0,
                                        EstadoDisponibilidadTpp.NO_DISPONIBLE,
                                        "SIN_ANALISIS_VALIDOS",
                                        Map.of()),
                                25.0,
                                null));
        MockMvc dashboard =
                MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        dashboard
                .perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pcc.porcentajePcc").value(50.0))
                .andExpect(jsonPath("$.pcs.porcentajePcs").value(nullValue()))
                .andExpect(jsonPath("$.tpp.promedioTppMs").value(nullValue()))
                .andExpect(jsonPath("$.porcentajeNivelConsumoOperativo").value(25.0))
                .andExpect(jsonPath("$.porcentajeBajoConocimiento").doesNotExist())
                .andExpect(jsonPath("$.porcentajePcsOficial").doesNotExist())
                .andExpect(jsonPath("$.tiempoPromedioPrediccion").doesNotExist());
    }
}
