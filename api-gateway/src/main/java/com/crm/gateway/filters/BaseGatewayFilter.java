package com.crm.gateway.filters;

import com.crm.sharedlib.exception.response.CrmErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public abstract class BaseGatewayFilter implements GatewayFilter {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected Mono<Void> handleWebClientError(
            ServerWebExchange exchange,
            WebClientResponseException e
    ) {
        exchange.getResponse().setStatusCode(e.getStatusCode());
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(e.getResponseBodyAsByteArray())
                )
        );
    }

    @SneakyThrows
    protected Mono<Void> respondWithError(ServerWebExchange exchange, int httpStatus, CrmErrorResponse body) {
        exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(httpStatus));
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(objectMapper.writeValueAsBytes(body))
                )
        );
    }

}
