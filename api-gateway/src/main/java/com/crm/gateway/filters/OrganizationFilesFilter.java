package com.crm.gateway.filters;

import com.crm.sharedlib.core.consts.CrmHeaders;
import com.crm.sharedlib.core.exception.response.CrmErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
    private final WebClient webClient;

    @Autowired
    public OrganizationFilesFilter(
            WebClient.Builder webClientBuilder,
            @Value("${app.clients.main-service.name}") String mainServiceName
    ) {
        this.webClient = webClientBuilder.baseUrl("lb://" + mainServiceName).build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String organizationId = request.getHeaders()
                .getFirst(CrmHeaders.ORGANIZATION_ID_HEADER_NAME);

        String userId = request.getHeaders()
                .getFirst(CrmHeaders.USER_ID_HEADER_NAME);

        if (isNull(organizationId)) {
            respondWithError(exchange, HttpStatus.FORBIDDEN, new CrmErrorResponse("Organization ID not specified"));

            return chain.filter(exchange);
        }

        UUID fileId = extractFileIdFromRequest(exchange);

        if (isNull(fileId)) {
            respondWithError(exchange, HttpStatus.FORBIDDEN, new CrmErrorResponse("File ID not specified"));

            return chain.filter(exchange);
        }

        return webClient
                .get()
                .uri("/api/internal/organizations/%d/files/%s/check".formatted(Long.valueOf(organizationId), fileId))
                .header(CrmHeaders.ORGANIZATION_ID_HEADER_NAME, organizationId)
                .header(CrmHeaders.USER_ID_HEADER_NAME, userId)
                .retrieve()
                .toBodilessEntity()
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
