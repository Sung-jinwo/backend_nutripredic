package com.backend.nutri_predic.ml.client;

import com.backend.nutri_predic.ml.dto.MlPredictRequest;
import com.backend.nutri_predic.ml.dto.MlPredictResponse;

public interface ModeloMlClient {
    MlPredictResponse predecir(MlPredictRequest request);
}
