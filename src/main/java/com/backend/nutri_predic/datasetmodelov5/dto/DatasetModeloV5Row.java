package com.backend.nutri_predic.datasetmodelov5.dto;

import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DatasetModeloV5Row(
        Metadata metadata, FeatureSchemaV5 features, String clasificacionReal) {
    public record Metadata(
            String caseId,
            UUID clienteGroupId,
            LocalDate fechaCorte,
            String schemaVersion,
            String rubricaCodigo,
            BigDecimal rubricaVersion,
            String fuentePerfil,
            boolean alimentacionCompleta,
            boolean suplementacionCompleta,
            boolean habitosCompletos) {}
}
