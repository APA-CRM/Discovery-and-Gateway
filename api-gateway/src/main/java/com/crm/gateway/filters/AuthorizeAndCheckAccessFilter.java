package com.crm.gateway.filters;

import com.crm.gateway.service.JwtService;
import com.crm.gateway.service.UserPermissionService;
import com.crm.gateway.utils.OrganizationIdExtractor;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import com.crm.sharedlib.rbac.dto.JwtPayload;
import com.crm.sharedlib.rbac.utils.UserPermissionHeaderSerializer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.crm.sharedlib.core.consts.CrmHeaders.*;
import static java.util.Objects.isNull;

@Component
public class AuthorizeAndCheckAccessFilter extends BaseGatewayFilter {

    private final UserPermissionService userPermissionService;

    public AuthorizeAndCheckAccessFilter(
            JwtService jwtService, UserPermissionService userPermissionService
    ) {
        super(jwtService);
        this.userPermissionService = userPermissionService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        Optional<JwtPayload> payloadOptional = getJwtPayload(request);
        if (payloadOptional.isEmpty()) {
            return respondWithError(exchange, HttpStatus.UNAUTHORIZED, new CrmErrorResponse("Unauthorized"));
        }

        JwtPayload payload = payloadOptional.get();

        Long organizationId = OrganizationIdExtractor.extractOrganizationIdFromRequest(exchange);
        if (isNull(organizationId)) {
            return respondWithError(exchange, HttpStatus.FORBIDDEN, new CrmErrorResponse("Forbidden"));
        }

        return userPermissionService.getUserPermission(payload.getId(), organizationId)
                .flatMap(userPermission -> {
                    ServerHttpRequest httpRequest = request.mutate()
                            .header(USER_ID_HEADER_NAME, payload.getId().toString())
                            .header(USER_LOGIN_HEADER_NAME, payload.getLogin())
                            .header(USER_PERMISSIONS_HEADER_NAME, UserPermissionHeaderSerializer.serialize(userPermission))
                            .build();

                    return chain.filter(exchange.mutate().request(httpRequest).build());
                })
                .onErrorResume(
                        WebClientResponseException.class,
                        ex -> handleWebClientError(exchange, ex)
                );
    }

}
