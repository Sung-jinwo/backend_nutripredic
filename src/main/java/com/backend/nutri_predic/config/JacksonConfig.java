package com.backend.nutri_predic.config;

import java.util.ArrayList;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JacksonConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
        builder.configureMessageConverters(
                candidate -> {
                    if (!(candidate instanceof AbstractJacksonHttpMessageConverter<?> converter)) return;
                    var mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
                    if (!mediaTypes.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                        converter.setSupportedMediaTypes(mediaTypes);
                    }
                });
    }
}
