package com.crm.gateway.cache;

import com.crm.sharedlib.rbac.constants.CacheConstants;
import com.crm.sharedlib.rbac.dto.UserPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class UserPermissionRepository {

    private final ReactiveRedisTemplate<String, UserPermission> redisTemplate;

    public Mono<UserPermission> getUserPermission(Long userId, Long organizationId) {
        return redisTemplate.opsForValue()
                .get(getFormatedKey(organizationId, userId));
    }


    private String getFormatedKey(Long organizationId, Long userId) {
        return CacheConstants.USER_PERMISSION_FORMAT_KEY.formatted(organizationId, userId);
    }

}
