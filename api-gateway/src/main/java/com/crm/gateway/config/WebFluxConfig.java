package com.crm.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Value("${cors.allowed.origins:*}")
    private String allowedOrigins;
    @Value("${cors.allowed.headers:*}")
    private String allowedHeaders;
    @Value("${cors.allowed.methods:*}")
    private String allowedMethods;

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders);
    }

}
