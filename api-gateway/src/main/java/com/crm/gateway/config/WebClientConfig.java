package com.crm.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final String HTTP_PREFIX = "http://";

    @Bean
    public WebClient authServiceWebClient(
            WebClient.Builder builder,
            @Value("${app.clients.auth-service.name}") String authServiceName
    ) {
        return builder.baseUrl(HTTP_PREFIX + authServiceName).build();
    }

    @Bean
    public WebClient mainServiceWebClient(
            WebClient.Builder builder,
            @Value("${app.clients.main-service.name}") String mainServiceName
    ) {
        return builder.baseUrl(HTTP_PREFIX + mainServiceName).build();
    }

}
