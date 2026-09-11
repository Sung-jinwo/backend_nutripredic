package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.ml.dto.MlProbabilidadesResponse;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import com.backend.nutri_predic.prediccionmodelo.controller.AnalisisPredictivoController;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoRequest;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoResponse;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AnalisisPredictivoControllerTests {
    @Mock ModeloPredictivoV6Service modelo;
    @Mock AccessService access;
    @Mock EventoAnalisisService eventos;
    @Mock Authentication authentication;

    @Test
    void endpointDirectoDelegaAlFlujoInstrumentado() {
        AnalisisPredictivoController controller =
                new AnalisisPredictivoController(modelo, access, eventos);
        AnalisisPredictivoRequest request =
                new AnalisisPredictivoRequest(
                        1L, LocalDate.of(2026, 8, 30), null, MomentoEvaluacion.BASAL);
        AnalisisPredictivoResponse esperada =
                new AnalisisPredictivoResponse(
                        8L,
                        9L,
                        request.fechaCorte(),
                        "BASAL",
                        "ADECUADO",
                        new MlProbabilidadesResponse(
                                new BigDecimal("0.8"),
                                new BigDecimal("0.15"),
                                new BigDecimal("0.05")),
                        "modelo-v1",
                        "schema-v1",
                        BigDecimal.TEN,
                        Instant.parse("2026-08-30T10:00:01Z"),
                        "GENERADO",
                        "VALIDA",
                        Instant.parse("2026-08-30T10:00:00Z"),
                        Instant.parse("2026-08-30T10:00:02Z"));
        when(eventos.predecirInstrumentadoV6(request, authentication)).thenReturn(esperada);

        var respuesta = controller.predecir(request, authentication);

        assertThat(respuesta).isSameAs(esperada);
        verify(eventos).predecirInstrumentadoV6(request, authentication);
        verifyNoInteractions(modelo);
    }
}
