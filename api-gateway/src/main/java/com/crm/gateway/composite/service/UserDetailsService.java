package com.crm.gateway.composite.service;

import com.crm.gateway.composite.auth.UserDetails;
import com.crm.gateway.service.JwtService;
import com.crm.gateway.service.UserPermissionService;
import com.crm.gateway.utils.JwtUtils;
import com.crm.sharedlib.core.exception.UnauthorizedException;
import com.crm.sharedlib.rbac.dto.JwtPayload;
import com.crm.sharedlib.rbac.utils.UserPermissionHeaderSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsService {

    private final UserPermissionService userPermissionService;

    private final JwtService jwtService;

    public Mono<UserDetails> getUserDetails(final String authHeader, Long organizationId) {
        Optional<String> tokenOptional = JwtUtils.getJwtTokenFromAuthorizationHeader(authHeader);

        String token = tokenOptional.orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        Optional<JwtPayload> payloadOptional = jwtService.getPayloadFromJwtToken(token);

        JwtPayload jwtPayload = payloadOptional.orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        return userPermissionService.getUserPermission(jwtPayload.getId(), organizationId)
                .map(userPermission ->
                        new UserDetails(
                                jwtPayload.getId(), organizationId,
                                UserPermissionHeaderSerializer.serialize(userPermission)
                        )
                );
    }

}
