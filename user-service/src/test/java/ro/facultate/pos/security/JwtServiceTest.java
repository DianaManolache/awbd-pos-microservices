package ro.facultate.pos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-for-jwt-signing-in-tests-32bytes-minimum";

    private final JwtService jwtService = new JwtService(SECRET, 15);

    @Test
    void generateToken_thenValidate_roundTripsClaims() {
        String token = jwtService.generateToken("ana.vanzatoare", "ADMIN");

        Claims claims = jwtService.validateAndGetClaims(token);

        assertEquals("ana.vanzatoare", claims.getSubject());
        assertEquals("ADMIN", claims.get("rol", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void validateAndGetClaims_tamperedSignature_throws() {
        String token = jwtService.generateToken("ana.vanzatoare", "USER");
        // schimba ultimul caracter din semnatura (ultima sectiune dupa punctul final)
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThrows(SignatureException.class, () -> jwtService.validateAndGetClaims(tampered));
    }

    @Test
    void validateAndGetClaims_signedWithDifferentSecret_throws() {
        JwtService otherService = new JwtService("alt-secret-complet-diferit-32bytes-minimum-lungime", 15);
        String token = otherService.generateToken("ana.vanzatoare", "USER");

        assertThrows(SignatureException.class, () -> jwtService.validateAndGetClaims(token));
    }

    @Test
    void validateAndGetClaims_expiredToken_throws() {
        JwtService shortLived = new JwtService(SECRET, 0);
        String token = shortLived.generateToken("ana.vanzatoare", "USER");

        assertThrows(ExpiredJwtException.class, () -> shortLived.validateAndGetClaims(token));
    }
}
