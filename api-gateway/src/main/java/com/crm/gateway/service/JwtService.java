package com.crm.gateway.service;

import com.crm.sharedlib.rbac.dto.JwtPayload;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

import static com.crm.sharedlib.rbac.constants.JwtConstants.USER_ID_KEY;
import static com.crm.sharedlib.rbac.constants.JwtConstants.USER_LOGIN_KEY;
import static java.util.Objects.isNull;

@Service
public class JwtService {

    @Value("${app.token.signing.key}")
    private String jwtSigningKey;

    public Optional<JwtPayload> getPayloadFromJwtToken(String token) {
        Claims claims = getClaims(token);

        if (isNull(claims) || isExpired(claims)) {
            return Optional.empty();
        }

        Long userId = Long.valueOf((String) claims.get(USER_ID_KEY));
        String userLogin = (String) claims.get(USER_LOGIN_KEY);

        return Optional.of(new JwtPayload(userId, userLogin));
    }

    @Nullable
    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSingingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    private SecretKey getSingingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
