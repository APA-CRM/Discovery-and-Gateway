package com.crm.gateway.config;

import com.crm.gateway.filters.AuthFilter;
import com.crm.gateway.filters.AuthorizeAndCheckAccessFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class RouteConfig {

    @Autowired
    private AuthFilter authFilter;

    @Autowired
    private AuthorizeAndCheckAccessFilter checkAccessFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "main-service-authorize", r -> r.path(
                                        "/api/organizations", "/api/organizations/invitations/**"
                                )
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://main-service")
                )
                .route("auth-service-authorize", r -> r.path(
                                        "/api/users/**"
                                ).and()
                                .method(HttpMethod.GET)
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://auth-service")
                )
                .route(
                        "main-service-check-access", r -> r.path(
                                        "/api/organizations/*/users/**",
                                    "/api/organizations/*/roles/**",
                                    "/api/organizations/*/users/*/roles/**",
                                    "/api/organizations/*/invitations"
                                )
                                .filters(spec -> spec.filter(checkAccessFilter))
                                .uri("lb://main-service")
                )
                .route(
                        "auth-service-check-access", r -> r.path(
                                        "/api/users/**",
                                        "/api/access-control/**"
                                )
                                .filters(spec -> spec.filter(checkAccessFilter))
                                .uri("lb://auth-service")
                )
                .route(
                        "authorization-api", r -> r.path("/api/auth/**")
                                .uri("lb://auth-service")
                )
                .build();
    }

}
