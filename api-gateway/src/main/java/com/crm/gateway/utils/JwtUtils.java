package com.crm.gateway.utils;

import lombok.experimental.UtilityClass;

import java.util.Optional;

@UtilityClass
public class JwtUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    public static Optional<String> getJwtTokenFromAuthorizationHeader(String authorizationHeader) {
        return Optional.ofNullable(authorizationHeader)
                .filter(str -> str.startsWith(BEARER_PREFIX))
                .map(str -> str.substring(BEARER_PREFIX.length()));
    }

}
