package com.crm.gateway.clients;

import com.crm.sharedlib.rbac.dto.UserPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceClient {

    private final WebClient webClient;

    @Autowired
    public AuthServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${app.clients.auth-service.name}") String authServiceName
    ) {
        this.webClient = webClientBuilder.baseUrl("lb://" + authServiceName).build();
    }

    public Mono<UserPermission> getUserPermissions(Long userId, Long organizationId) {
        return webClient
                .get()
                .uri("/users/{userId}/organizations/{organizationId}/persmissions",
                        userId, organizationId)
                .retrieve()
                .bodyToMono(UserPermission.class);
    }

}
