package com.crm.config;

import com.crm.filters.AuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Autowired
    private AuthFilter authFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        "main-service", r -> r.path(
                                        "/api/organizations/**"
                                )
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://main-service")
                )
                .route(
                        "auth-service", r -> r.path(
                                        "/api/users/**",
                                        "/api/access-control/**"
                                )
                                .filters(spec -> spec.filter(authFilter))
                                .uri("lb://auth-service")
                )
                .route(
                        "authorization-api", r -> r.path("/api/auth/**")
                                .uri("lb://auth-service")
                )
                .build();
    }

}
