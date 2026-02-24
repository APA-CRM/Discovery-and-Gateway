package com.crm.gateway.filters;

import com.crm.gateway.utils.JwtUtils;
import com.crm.sharedlib.core.dto.request.AuthorizationRequest;
import com.crm.sharedlib.core.dto.response.AuthResponse;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.crm.sharedlib.core.consts.CrmHeaders.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.core.consts.CrmHeaders.USER_LOGIN_HEADER_NAME;

@Component
public class AuthFilter extends BaseGatewayFilter {

    private final WebClient webClient;

    @Autowired
    public AuthFilter(
            WebClient.Builder webClientBuilder,
            @Value("${app.clients.auth-service.name}") String authServiceName
    ) {
        this.webClient = webClientBuilder.baseUrl("lb://" + authServiceName).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String userAgent = request.getHeaders().getFirst(HttpHeaders.USER_AGENT);

        Optional<String> tokenOptional = JwtUtils.getJwtTokenFromAuthorizationHeader(authHeader);

        if (tokenOptional.isEmpty()) {
            return respondWithError(exchange, 401, new CrmErrorResponse("Unauthorized"));
        }

        AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(tokenOptional.get(), userAgent);

        return webClient
                .post()
                .uri("/api/internal/auth/authorize")
                .bodyValue(authorizationRequest)
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

}
