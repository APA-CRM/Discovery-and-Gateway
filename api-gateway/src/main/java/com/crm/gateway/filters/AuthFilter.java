package com.crm.gateway.filters;

import com.crm.sharedlib.dto.response.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.crm.sharedlib.consts.CrmConstants.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.consts.CrmConstants.USER_LOGIN_HEADER_NAME;

@Component
public class AuthFilter implements GatewayFilter {

    private final WebClient webClient;

    @Autowired
    public AuthFilter(
            WebClient.Builder webClientBuilder,
            @Value("${app.auth.url}") String authUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(authUrl).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        return webClient
                .get()
                .uri("/api/auth/authorize")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(AuthResponse.class)
                .flatMap(authResponse -> {
                    ServerHttpRequest httpRequest = request.mutate()
                            .header(USER_ID_HEADER_NAME, authResponse.getId().toString())
                            .header(USER_LOGIN_HEADER_NAME, authResponse.getLogin())
                            .build();

                    return chain.filter(exchange.mutate().request(httpRequest).build());
                })
                .onErrorResume(WebClientResponseException.class, ex -> handleWebClientError(exchange, ex));
    }

    private Mono<Void> handleWebClientError(
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

}
