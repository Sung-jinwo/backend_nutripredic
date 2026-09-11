package com.backend.nutri_predic.variablemodelov5.schema;

import java.util.Map;

public record FeatureSchemaV5(String schemaVersion, Map<String, Object> features) {}
