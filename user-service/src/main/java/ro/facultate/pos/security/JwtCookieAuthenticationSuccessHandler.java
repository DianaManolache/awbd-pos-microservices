package ro.facultate.pos.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * La login cu succes (sesiune + CSRF raman exact ca inainte), emite si un JWT
 * si il pune intr-un cookie separat (AUTH_TOKEN). Gateway-ul citeste acest
 * cookie si il ataseaza ca header Authorization pe cererile proxy-ate catre
 * Catalog/Sales, care nu au propria lor sesiune si nu pot autentifica altfel.
 */
@Component
public class JwtCookieAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    public static final String AUTH_COOKIE_NAME = "AUTH_TOKEN";

    private final JwtService jwtService;
    private final AuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    public JwtCookieAuthenticationSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        String rol = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("USER");

        String token = jwtService.generateToken(authentication.getName(), rol);

        Cookie cookie = new Cookie(AUTH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(15 * 60);
        response.addCookie(cookie);

        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
