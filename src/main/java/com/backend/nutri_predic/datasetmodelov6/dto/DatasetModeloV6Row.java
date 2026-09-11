package com.backend.nutri_predic.datasetmodelov6.dto;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.UUID;
public record DatasetModeloV6Row(Metadata metadata,FeatureSchemaV6 features,String clasificacionReal){public record Metadata(String caseId,UUID clienteGroupId,LocalDate fechaCorte,String schemaVersion,String rubricaCodigo,BigDecimal rubricaVersion,String fuentePerfil){}}
