package com.backend.nutri_predic.ml.client;

import com.backend.nutri_predic.ml.dto.MlNutritionTargetsRequest;
import com.backend.nutri_predic.ml.dto.MlNutritionTargetsResponse;

public interface MetasNutricionalesMlClient {
    MlNutritionTargetsResponse calcular(MlNutritionTargetsRequest request);
}
