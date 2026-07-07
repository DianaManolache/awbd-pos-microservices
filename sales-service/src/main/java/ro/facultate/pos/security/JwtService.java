package ro.facultate.pos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Valideaza JWT-uri semnate cu secretul partajat (primit din Config Server).
 * Sales Service nu emite token-uri la login (nu are utilizatori proprii),
 * dar poate genera un token de sistem pentru apeluri Feign fara context de
 * cerere curenta (vezi FeignAuthInterceptor).
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(@Value("${app.jwt.secret:test-only-fallback-secret-never-used-in-dev-or-prod-min32chars}") String secret,
                       @Value("${app.jwt.expiration-minutes:15}") long expirationMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username, String rol) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirationMinutes * 60)))
                .signWith(key)
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
