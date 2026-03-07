package com.crm.gateway.filters;

import com.crm.gateway.service.JwtService;
import com.crm.gateway.utils.JwtUtils;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import com.crm.sharedlib.rbac.dto.JwtPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

public abstract class BaseGatewayFilter implements GatewayFilter {

    protected final JwtService jwtService;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    BaseGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

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
    protected Mono<Void> respondWithError(ServerWebExchange exchange, HttpStatusCode httpStatus, CrmErrorResponse body) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(objectMapper.writeValueAsBytes(body))
                )
        );
    }

    protected Optional<JwtPayload> getJwtPayload(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        Optional<String> tokenOptional = JwtUtils.getJwtTokenFromAuthorizationHeader(authHeader);

        if (tokenOptional.isEmpty()) {
            return Optional.empty();
        }

        return jwtService.getPayloadFromJwtToken(tokenOptional.get());
    }

}
