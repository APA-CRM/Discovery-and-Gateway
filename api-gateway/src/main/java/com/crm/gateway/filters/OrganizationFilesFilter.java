package com.crm.gateway.filters;

import com.crm.gateway.clients.OrganizationFilesClient;
import com.crm.gateway.service.JwtService;
import com.crm.sharedlib.core.consts.CrmHeaders;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;

@Component
public class OrganizationFilesFilter extends BaseGatewayFilter {

    private static final Pattern PATTERN = Pattern.compile("^/api/files/(?<fileId>[0-9a-fA-F]{8}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{4}\\b-[0-9a-fA-F]{12})(/.*)?$");
    private final OrganizationFilesClient organizationFilesClient;

    @Autowired
    public OrganizationFilesFilter(
            JwtService jwtService, OrganizationFilesClient organizationFilesClient
    ) {
        super(jwtService);
        this.organizationFilesClient = organizationFilesClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String organizationId = request.getHeaders()
                .getFirst(CrmHeaders.ORGANIZATION_ID_HEADER_NAME);

        String userId = request.getHeaders()
                .getFirst(CrmHeaders.USER_ID_HEADER_NAME);

        if (isNull(userId)) {
            return respondWithError(exchange, HttpStatus.FORBIDDEN, new CrmErrorResponse("User ID not specified"));
        }

        if (isNull(organizationId)) {
            return respondWithError(exchange, HttpStatus.FORBIDDEN, new CrmErrorResponse("Organization ID not specified"));
        }

        UUID fileId = extractFileIdFromRequest(exchange);

        if (isNull(fileId)) {
            return respondWithError(exchange, HttpStatus.FORBIDDEN, new CrmErrorResponse("File ID not specified"));
        }

        return organizationFilesClient.checkFileExistenceInOrganization(
                        Long.valueOf(organizationId), fileId, Long.valueOf(userId)
                )
                .flatMap(resp -> chain.filter(exchange))
                .onErrorResume(WebClientResponseException.class, ex -> handleWebClientError(exchange, ex));
    }

    private UUID extractFileIdFromRequest(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();

        Matcher matcher = PATTERN.matcher(path);

        if (matcher.matches()) {
            return UUID.fromString(matcher.group("fileId"));
        }

        return null;
    }
}
