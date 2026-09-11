package com.backend.nutri_predic.ml.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "nutripredict.ml")
public class ModeloMlProperties {
    @NotBlank private String baseUrl = "http://localhost:8000";
    @NotBlank private String predictPath = "/predict";
    @NotNull private Duration timeout = Duration.ofSeconds(5);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String value) {
        baseUrl = value;
    }

    public String getPredictPath() {
        return predictPath;
    }

    public void setPredictPath(String value) {
        predictPath = value;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration value) {
        timeout = value;
    }
}
