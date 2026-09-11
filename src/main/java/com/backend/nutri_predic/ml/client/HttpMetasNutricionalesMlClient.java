package com.backend.nutri_predic.ml.client;

import com.backend.nutri_predic.ml.config.ModeloMlProperties;
import com.backend.nutri_predic.ml.dto.MlNutritionTargetsRequest;
import com.backend.nutri_predic.ml.dto.MlNutritionTargetsResponse;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpMetasNutricionalesMlClient implements MetasNutricionalesMlClient {
    private final RestClient client;

    public HttpMetasNutricionalesMlClient(
            RestClient.Builder builder, ModeloMlProperties properties) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeout());
        factory.setReadTimeout(properties.getTimeout());
        client = builder.baseUrl(properties.getBaseUrl()).requestFactory(factory).build();
    }

    @Override
    public MlNutritionTargetsResponse calcular(MlNutritionTargetsRequest request) {
        try {
            var response = client.post()
                    .uri("/nutrition-targets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(MlNutritionTargetsResponse.class);
            if (response == null || response.estado() == null) {
                throw new ModeloMlException("El servicio ML no devolvió metas nutricionales");
            }
            return response;
        } catch (ModeloMlException error) {
            throw error;
        } catch (RestClientException error) {
            throw new ModeloMlException(
                    "No se pudo calcular el plan inicial con el servicio ML", error);
        }
    }
}
