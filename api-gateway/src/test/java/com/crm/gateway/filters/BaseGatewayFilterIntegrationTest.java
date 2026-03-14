package com.crm.gateway.filters;

import com.crm.gateway.BaseIntegrationTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.crm.sharedlib.rbac.constants.JwtConstants.USER_ID_KEY;
import static com.crm.sharedlib.rbac.constants.JwtConstants.USER_LOGIN_KEY;

@Import(BaseGatewayFilterIntegrationTest.GatewayTestConfiguration.class)
public class BaseGatewayFilterIntegrationTest extends BaseIntegrationTest {

    private static final int EXPIRATION_TIME_IN_MINUTES = 10;
    @Value("${app.token.signing.key}")
    private String jwtSigningKey;

    protected String getAuthorizationHeaderValue(String accessToken) {
        return "Bearer " + accessToken;
    }

    protected String generateToken(Long userId, String login) {
        return Jwts.builder()
                .claim(USER_ID_KEY, userId.toString())
                .claim(USER_LOGIN_KEY, login)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(EXPIRATION_TIME_IN_MINUTES)))
                .signWith(getSingingKey())
                .compact();
    }

    private SecretKey getSingingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @TestConfiguration
    public static class GatewayTestConfiguration {

        @Bean
        ServiceInstanceListSupplier backendServicesInstanceSupplier() {
            return ServiceInstanceListSuppliers.from(
                    "backend-services",
                    new DefaultServiceInstance(
                            "main-1",
                            "main-service",
                            "localhost",
                            wireMock.port(),
                            false
                    ),
                    new DefaultServiceInstance(
                            "auth-1",
                            "auth-service",
                            "localhost",
                            wireMock.port(),
                            false
                    ),
                    new DefaultServiceInstance(
                            "file-1",
                            "file-service",
                            "localhost",
                            wireMock.port(),
                            false
                    ),
                    new DefaultServiceInstance(
                            "notification-1",
                            "notification-service",
                            "localhost",
                            wireMock.port(),
                            false
                    )
            );
        }

    }

}
