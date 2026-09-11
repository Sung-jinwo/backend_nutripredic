package com.backend.nutri_predic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.backend.nutri_predic.common.exception.GlobalExceptionHandler;
import com.backend.nutri_predic.conocimiento.practica.controller.ConocimientoPostModeloController;
import com.backend.nutri_predic.conocimiento.practica.dto.SesionConocimientoResultadoResponse;
import com.backend.nutri_predic.conocimiento.practica.dto.SesionConocimientoResultadoResponse.ResultadoRespuesta;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import com.backend.nutri_predic.conocimiento.practica.service.RespuestaConocimientoIaService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ConocimientoIaRespuestaHttpContractTests {
    private final GeneracionPreguntasConocimientoService generacion =
            mock(GeneracionPreguntasConocimientoService.class);
    private final RespuestaConocimientoIaService respuestas =
            mock(RespuestaConocimientoIaService.class);
    private final MockMvc mvc =
            MockMvcBuilders.standaloneSetup(
                            new ConocimientoPostModeloController(generacion, respuestas))
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();

    @Test
    void respuestaValidaExponeFeedbackSinNivelPcc() throws Exception {
        when(respuestas.responder(any(), any(), any(), any()))
                .thenReturn(
                        new SesionConocimientoResultadoResponse(
                                10L,
                                2,
                                2,
                                1,
                                50.0,
                                "RESPONDIDA",
                                Instant.parse("2026-08-31T10:00:00Z"),
                                List.of(
                                        new ResultadoRespuesta(
                                                101L, "B", true, "B", "Feedback educativo"))));

        mvc.perform(
                        post("/api/clientes/1/conocimiento/post-modelo/10/respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"respuestas":[
                                          {"preguntaId":101,"opcionSeleccionada":"B"},
                                          {"preguntaId":102,"opcionSeleccionada":"A"}
                                        ]}
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESPONDIDA"))
                .andExpect(jsonPath("$.porcentajeAdaptativo").value(50.0))
                .andExpect(jsonPath("$.respuestas[0].respuestaCorrecta").value("B"))
                .andExpect(jsonPath("$.respuestas[0].explicacion").exists())
                .andExpect(jsonPath("$.nivelPcc").doesNotExist())
                .andExpect(jsonPath("$.nivel").doesNotExist());
    }

    @Test
    void camposDeSolucionEnviadosPorClienteSonRechazados() throws Exception {
        mvc.perform(
                        post("/api/clientes/1/conocimiento/post-modelo/10/respuestas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"respuestas":[{
                                          "preguntaId":101,
                                          "opcionSeleccionada":"B",
                                          "respuestaCorrecta":"B"
                                        }]}
                                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(respuestas);
    }
}
