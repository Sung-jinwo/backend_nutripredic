package com.backend.nutri_predic.variablemodelo.schema;

import java.util.Map;

public record FeatureSchemaV4(String schemaVersion, Map<String, Object> features) {}
