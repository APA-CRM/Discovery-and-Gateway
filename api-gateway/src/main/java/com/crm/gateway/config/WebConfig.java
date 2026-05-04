package com.crm.gateway.config;

import com.crm.sharedlib.core.exception.factory.HttpExceptionFactory;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig {

    @Bean
    @LoadBalanced
    WebClient.Builder builder() {
        return WebClient.builder()
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(CrmErrorResponse.class)
                        .map(crmErrorResponse -> HttpExceptionFactory.of(
                                clientResponse.statusCode().value(), crmErrorResponse.getMessage()
                        )));
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }

}
