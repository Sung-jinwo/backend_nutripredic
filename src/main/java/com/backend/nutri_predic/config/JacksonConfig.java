package com.backend.nutri_predic.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.AbstractJacksonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JacksonConfig implements WebMvcConfigurer {
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(AbstractJacksonHttpMessageConverter.class::isInstance)
                .map(AbstractJacksonHttpMessageConverter.class::cast)
                .forEach(
                        converter -> {
                            var mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
                            if (!mediaTypes.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                                mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                                converter.setSupportedMediaTypes(mediaTypes);
                            }
                        });
    }
}
