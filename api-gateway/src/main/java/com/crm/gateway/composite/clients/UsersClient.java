package com.crm.gateway.composite.clients;

import com.crm.gateway.composite.dtos.response.users.UserLightResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Component
public class UsersClient {

    private static final String INTERNAL_USERS = "/api/internal/users";

    private static final String USER_ID_QUERY_PARAM_NAME = "userId";

    private final WebClient webClient;

    public UsersClient(@Qualifier("authServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<UserLightResponse> getUsersByIds(Collection<Long> usersIds) {
        return webClient.get()
                .uri(INTERNAL_USERS, uriBuilder -> uriBuilder
                        .queryParam(USER_ID_QUERY_PARAM_NAME, usersIds)
                        .build()
                )
                .retrieve()
                .bodyToFlux(UserLightResponse.class);
    }

}
