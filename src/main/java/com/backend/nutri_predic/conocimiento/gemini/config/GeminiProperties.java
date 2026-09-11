package com.backend.nutri_predic.conocimiento.gemini.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.gemini")
public class GeminiProperties {
    private String apiKey;
    private String baseUrl = "https://generativelanguage.googleapis.com";
    private String model = "gemini-2.5-flash-lite";
    private Duration timeout = Duration.ofSeconds(10);
    private boolean mockEnabled = true;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String v) {
        apiKey = v;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String v) {
        baseUrl = v;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String v) {
        model = v;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration v) {
        timeout = v;
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean v) {
        mockEnabled = v;
    }
}
