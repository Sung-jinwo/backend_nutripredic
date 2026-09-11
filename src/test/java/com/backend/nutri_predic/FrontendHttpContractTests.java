package com.backend.nutri_predic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.common.exception.GlobalExceptionHandler;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.ml.dto.MlProbabilidadesResponse;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import com.backend.nutri_predic.prediccionmodelo.controller.AnalisisPredictivoController;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoResponse;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FrontendHttpContractTests {
    private final EventoAnalisisService eventos = mock(EventoAnalisisService.class);
    private final MockMvc mvc =
            MockMvcBuilders.standaloneSetup(
                            new AnalisisPredictivoController(
                                    mock(ModeloPredictivoV6Service.class),
                                    mock(AccessService.class),
                                    eventos))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();

    @Test
    void analisisV6ExitosoExponeContratoSinCamposPlanosLegacy() throws Exception {
        when(eventos.predecirInstrumentadoV6(any(), any())).thenReturn(respuesta("GENERADO"));

        mvc.perform(
                        post("/api/analisis-predictivo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitud()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediccionId").value(81))
                .andExpect(jsonPath("$.eventoAnalisisId").value(91))
                .andExpect(jsonPath("$.clasificacion").value("ADECUADO"))
                .andExpect(jsonPath("$.probabilidades.ADECUADO").value(0.8))
                .andExpect(jsonPath("$.probabilidades.MEJORABLE").value(0.15))
                .andExpect(jsonPath("$.probabilidades.CRITICO").value(0.05))
                .andExpect(jsonPath("$.modelVersion").value("technical-v6-integration-001"))
                .andExpect(jsonPath("$.schemaVersion").value("variables-modelo-v6"))
                .andExpect(jsonPath("$.inferredAt").exists())
                .andExpect(jsonPath("$.origenResultado").value("GENERADO"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.probAdecuado").doesNotExist())
                .andExpect(jsonPath("$.tiempoPromedioPrediccion").doesNotExist());
    }

    @Test
    void respuestaReutilizadaSeInformaSinCambiarElContrato() throws Exception {
        when(eventos.predecirInstrumentadoV6(any(), any())).thenReturn(respuesta("REUTILIZADO"));

        mvc.perform(
                        post("/api/analisis-predictivo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitud()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origenResultado").value("REUTILIZADO"))
                .andExpect(jsonPath("$.estado").value("VALIDA"));
    }

    @Test
    void modeloNoDisponibleUsaContratoDeErrorComun() throws Exception {
        when(eventos.predecirInstrumentadoV6(any(), any()))
                .thenThrow(new ModeloMlException("Modelo no disponible"));

        mvc.perform(
                        post("/api/analisis-predictivo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(solicitud()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Modelo no disponible"))
                .andExpect(jsonPath("$.path").value("/api/analisis-predictivo"));
    }

    private String solicitud() {
        return """
                {
                  "clienteId": 42,
                  "fechaCorte": "2026-08-30",
                  "momento": "BASAL"
                }
                """;
    }

    private AnalisisPredictivoResponse respuesta(String origen) {
        return new AnalisisPredictivoResponse(
                81L,
                91L,
                LocalDate.of(2026, 8, 30),
                "BASAL",
                "ADECUADO",
                new MlProbabilidadesResponse(
                        new BigDecimal("0.8"), new BigDecimal("0.15"), new BigDecimal("0.05")),
                "technical-v6-integration-001",
                "variables-modelo-v6",
                new BigDecimal("12.4"),
                Instant.parse("2026-08-30T10:00:01Z"),
                origen,
                "VALIDA",
                Instant.parse("2026-08-30T10:00:00Z"),
                Instant.parse("2026-08-30T10:00:02Z"));
    }
}
