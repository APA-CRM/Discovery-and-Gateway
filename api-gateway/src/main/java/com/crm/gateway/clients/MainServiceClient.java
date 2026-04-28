package com.crm.gateway.clients;

import com.crm.sharedlib.core.consts.CrmHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class MainServiceClient {

    private final WebClient webClient;

    public MainServiceClient(@Qualifier("mainServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ResponseEntity<Void>> checkFileExistenceInOrganization(
            Long organizationId, UUID fileId, Long userId
    ) {
        return webClient
                .get()
                .uri("/api/internal/organizations/{organizationId}/files/{fileId}/check", organizationId, fileId)
                .header(CrmHeaders.ORGANIZATION_ID_HEADER_NAME, organizationId.toString())
                .header(CrmHeaders.USER_ID_HEADER_NAME, userId.toString())
                .retrieve()
                .toBodilessEntity();
    }

}
