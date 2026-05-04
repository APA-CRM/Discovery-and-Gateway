package com.crm.gateway.clients;

import com.crm.sharedlib.rbac.dto.UserPermission;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserPermissionClient {

    protected static final String USERS_USER_ID_ORGANIZATIONS_ORGANIZATION_ID_PERMISSIONS
            = "/users/{userId}/organizations/{organizationId}/persmissions";

    private final WebClient webClient;

    public UserPermissionClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<UserPermission> getUserPermissions(Long userId, Long organizationId) {
        return webClient
                .get()
                .uri(USERS_USER_ID_ORGANIZATIONS_ORGANIZATION_ID_PERMISSIONS,
                        userId, organizationId)
                .retrieve()
                .bodyToMono(UserPermission.class);
    }

}
