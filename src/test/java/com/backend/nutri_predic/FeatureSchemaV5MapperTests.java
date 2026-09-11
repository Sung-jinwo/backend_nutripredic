package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;

import com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FeatureSchemaV5MapperTests {
    @Test
    void conservaLasVeintiunaFeaturesEnElOrdenCongeladoYSinImputarNulos() {
        var features =
                new VariablesModeloV5Response.Features(
                        29,
                        new BigDecimal("70"),
                        new BigDecimal("175"),
                        "GANAR_MASA",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new BigDecimal("3"),
                        null,
                        null,
                        null,
                        null);
        var response =
                new VariablesModeloV5Response(
                        "variables-modelo-v5",
                        LocalDate.of(2026, 8, 28),
                        features,
                        new VariablesModeloV5Response.Metadata(
                                1L, null, "HISTORIAL", true, true, true));

        var schema = new FeatureSchemaV5Mapper().mapear(response);

        assertThat(schema.schemaVersion()).isEqualTo("variables-modelo-v5");
        assertThat(schema.features().keySet())
                .containsExactlyElementsOf(FeatureSchemaV5Mapper.FEATURE_NAMES);
        assertThat(schema.features()).hasSize(21);
        assertThat(schema.features().get("promedio_kcal_7d")).isNull();
        assertThat(schema.features().get("promedio_cantidad_comidas_7d"))
                .isEqualTo(new BigDecimal("3"));
    }
}
