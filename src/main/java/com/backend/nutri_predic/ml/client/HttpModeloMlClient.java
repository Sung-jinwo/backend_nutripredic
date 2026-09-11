package com.backend.nutri_predic.ml.client;

import com.backend.nutri_predic.ml.config.ModeloMlProperties;
import com.backend.nutri_predic.ml.dto.MlPredictRequest;
import com.backend.nutri_predic.ml.dto.MlPredictResponse;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpModeloMlClient implements ModeloMlClient {
    private final RestClient client;
    private final ModeloMlProperties properties;

    @Autowired
    public HttpModeloMlClient(RestClient.Builder builder, ModeloMlProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeout());
        factory.setReadTimeout(properties.getTimeout());
        this.client = builder.baseUrl(properties.getBaseUrl()).requestFactory(factory).build();
    }

    public HttpModeloMlClient(RestClient client, ModeloMlProperties properties) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public MlPredictResponse predecir(MlPredictRequest request) {
        try {
            MlPredictResponse response =
                    client.post()
                            .uri(properties.getPredictPath())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(request)
                            .retrieve()
                            .body(MlPredictResponse.class);
            MlPredictResponseValidator.validar(response, request.schemaVersion());
            return response;
        } catch (ModeloMlException error) {
            throw error;
        } catch (RestClientException error) {
            throw new ModeloMlException("No se pudo comunicar con el servicio ML", error);
        } catch (RuntimeException error) {
            throw new ModeloMlException("Respuesta inválida del servicio ML", error);
        }
    }
}
