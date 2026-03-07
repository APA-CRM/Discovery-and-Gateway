package com.crm.gateway.filters;

import com.crm.gateway.service.JwtService;
import com.crm.gateway.utils.JwtUtils;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import com.crm.sharedlib.rbac.dto.JwtPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.crm.sharedlib.core.consts.CrmHeaders.USER_ID_HEADER_NAME;
import static com.crm.sharedlib.core.consts.CrmHeaders.USER_LOGIN_HEADER_NAME;
import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class AuthFilter extends BaseGatewayFilter {

    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        Optional<String> tokenOptional = JwtUtils.getJwtTokenFromAuthorizationHeader(authHeader);

        if (tokenOptional.isEmpty()) {
            return respondWithError(exchange, HttpStatus.UNAUTHORIZED, new CrmErrorResponse("Unauthorized"));
        }

        Optional<JwtPayload> payloadOptional = jwtService.getPayloadFromJwtToken(tokenOptional.get());

        if (payloadOptional.isEmpty()) {
            return respondWithError(exchange, HttpStatus.UNAUTHORIZED, new CrmErrorResponse("Unauthorized"));
        }

        JwtPayload payload = payloadOptional.get();

        ServerHttpRequest httpRequest = request.mutate()
                .header(USER_ID_HEADER_NAME, payload.getId().toString())
                .header(USER_LOGIN_HEADER_NAME, payload.getLogin())
                .build();

        return chain.filter(exchange.mutate().request(httpRequest).build());
    }

}
