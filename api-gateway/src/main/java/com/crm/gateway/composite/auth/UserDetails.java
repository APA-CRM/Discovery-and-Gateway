package com.crm.gateway.composite.auth;

public record UserDetails(Long userId, Long organizationId, String permissions) {

}
