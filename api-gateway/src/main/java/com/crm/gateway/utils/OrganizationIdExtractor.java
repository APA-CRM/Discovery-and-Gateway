package com.crm.gateway.utils;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerWebExchange;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.crm.sharedlib.core.consts.CrmHeaders.ORGANIZATION_ID_HEADER_NAME;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@UtilityClass
public class OrganizationIdExtractor {

    private static final Pattern PATTERN = Pattern.compile("^/api/organizations/(?<organizationId>\\d+)(/.*)?$");

    @Nullable
    public static Long extractOrganizationIdFromRequest(ServerWebExchange webExchange) {
        Long organizationId = extractFromUri(webExchange.getRequest().getURI().toString());

        if (isNull(organizationId)) {
            return extractFromHeaders(webExchange);
        }

        return organizationId;
    }

    private static Long extractFromUri(String uri) {
        Matcher matcher = PATTERN.matcher(uri);

        if (matcher.matches()) {
            return Long.valueOf(matcher.group("organizationId"));
        }

        return null;
    }


    private static Long extractFromHeaders(ServerWebExchange webExchange) {
        String organizationId = webExchange.getRequest().getHeaders().getFirst(ORGANIZATION_ID_HEADER_NAME);

        return nonNull(organizationId) ? Long.valueOf(organizationId) : null;
    }
}
