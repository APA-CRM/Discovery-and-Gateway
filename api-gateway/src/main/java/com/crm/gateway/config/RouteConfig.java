package com.crm.gateway.config;

import com.crm.gateway.filters.AuthFilter;
import com.crm.gateway.filters.AuthorizeAndCheckAccessFilter;
import com.crm.gateway.filters.OrganizationFilesFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.clients.auth-service.name}")
    private String authServiceName;
    @Value("${app.clients.main-service.name}")
    private String mainServiceName;
    @Value("${app.clients.file-service.name}")
    private String fileServiceName;
    @Value("${app.clients.notification-service.name}")
    private String notificationServiceName;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("notifications-service-web-sockets", r -> r
                        .path("/ws-notifications/**")
                        .uri("lb:ws://" + notificationServiceName)
                )
                .route(
                        "main-service-authorize", r -> r.path(
                                        "/api/organizations",
                                        "/api/organizations/*", "/api/organizations/invitations/**"
                                ).and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.OPTIONS)
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://" + mainServiceName)
                )
                .route("auth-service-authorize", r -> r.path(
                                "/api/users/**"
                                )
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://" + authServiceName)
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
                                .uri("lb://" + mainServiceName)
                )
                .route(
                        "file-service-check-access-and-file-existence", r -> r.path(
                                        "/api/files/**"
                                ).and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PATCH, HttpMethod.OPTIONS)
                                .filters(spec -> spec.filters(checkAccessFilter, organizationFilesFilter))
                                .uri("lb://" + fileServiceName)
                )
                .route(
                        "file-service-check-access", r -> r.path(
                                        "/api/files/**"
                                ).and().method(HttpMethod.DELETE, HttpMethod.OPTIONS)
                                .filters(spec -> spec.filters(checkAccessFilter))
                                .uri("lb://" + fileServiceName)
                )
                .route(
                        "authorization-api", r -> r.path(
                                "/api/auth/**", "/api/restore-password-request/**"
                                )
                                .uri("lb://" + authServiceName)
                )
                .build();
    }

}
