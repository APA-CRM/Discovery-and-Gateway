package com.crm.gateway.service;

import com.crm.gateway.cache.UserPermissionRepository;
import com.crm.gateway.clients.UserPermissionClient;
import com.crm.sharedlib.rbac.dto.UserPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;

    private final UserPermissionClient userPermissionClient;

    public Mono<UserPermission> getUserPermission(Long userId, Long organizationId) {
        return userPermissionRepository.getUserPermission(userId, organizationId)
                .switchIfEmpty(userPermissionClient.getUserPermissions(userId, organizationId));
    }

}
