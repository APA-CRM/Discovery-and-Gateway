package com.crm.gateway.config;

import com.crm.gateway.filters.AuthFilter;
import com.crm.gateway.filters.AuthorizeAndCheckAccessFilter;
import com.crm.gateway.filters.OrganizationFilesFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class RouteConfig {

    private final AuthFilter authFilter;
    private final AuthorizeAndCheckAccessFilter checkAccessFilter;
    private final OrganizationFilesFilter organizationFilesFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("notifications-service-web-sockets", r -> r
                        .path("/ws-notifications/**")
                        .uri("lb:ws://notification-service")
                )
                .route("notifications-service-open", r -> r
                        .path("/api/test/notifications/**")
                        .uri("lb:ws://notification-service")
                )
                .route(
                        "main-service-authorize", r -> r.path(
                                        "/api/organizations",
                                        "/api/organizations/*", "/api/organizations/invitations/**"
                                ).and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.OPTIONS)
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://main-service")
                )
                .route("auth-service-authorize", r -> r.path(
                                        "/api/users/**"
                                )
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://auth-service")
                )
                .route(
                        "main-service-check-access", r -> r.path(
                                        "/api/organizations/*",
                                        "/api/organizations/*/users/**",
                                        "/api/organizations/*/roles/**",
                                        "/api/organizations/*/users/*/roles/**",
                                        "/api/organizations/*/invitations",
                                        "/api/organizations/*/files/**"
                                )
                                .filters(spec -> spec.filter(checkAccessFilter))
                                .uri("lb://main-service")
                )
                .route(
                        "file-service-check-access-and-file-existence", r -> r.path(
                                        "/api/files/**"
                                ).and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.OPTIONS)
                                .filters(spec -> spec.filters(checkAccessFilter, organizationFilesFilter))
                                .uri("lb://file-service")
                )
                .route(
                        "file-service-check-access", r -> r.path(
                                        "/api/files/**"
                                ).and().method(HttpMethod.DELETE, HttpMethod.OPTIONS)
                                .filters(spec -> spec.filters(checkAccessFilter))
                                .uri("lb://file-service")
                )
                .route(
                        "authorization-api", r -> r.path("/api/auth/**")
                                .uri("lb://auth-service")
                )
                .build();
    }

}
