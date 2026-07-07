package ro.facultate.pos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Valideaza un JWT din header-ul Authorization: Bearer si populeaza
 * SecurityContext-ul, DOAR daca acesta nu e deja populat (de exemplu de o
 * sesiune valida restaurata mai devreme in filter chain, cazul User Service).
 * Un JWT lipsa sau invalid nu blocheaza cererea aici - lasa `authorizeHttpRequests`
 * sa decida (401/403), exact cum se comporta si autentificarea prin sesiune.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                try {
                    Claims claims = jwtService.validateAndGetClaims(header.substring(7));
                    String username = claims.getSubject();
                    String rol = claims.get("rol", String.class);

                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                    var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (JwtException | IllegalArgumentException e) {
                    logger.debug("JWT invalid sau expirat: " + e.getMessage());
                }
            }
        }

        chain.doFilter(request, response);
    }
}
