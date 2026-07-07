package ro.facultate.pos.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propaga header-ul Authorization al cererii curente pe apelurile Feign de
 * iesire (astfel incat Sales Service sa vada acelasi utilizator/rol care a
 * declansat cererea). Cand nu exista o cerere curenta (ex. AdminSeeder,
 * rulat la pornire ca CommandLineRunner, fara nicio cerere HTTP in curs),
 * genereaza un token de sistem cu rol ADMIN, folosit doar pentru acel apel
 * de bootstrap.
 */
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private final JwtService jwtService;

    public FeignAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                template.header("Authorization", authHeader);
                return;
            }
        }

        String systemToken = jwtService.generateToken("system-bootstrap", "ADMIN");
        template.header("Authorization", "Bearer " + systemToken);
    }
}
