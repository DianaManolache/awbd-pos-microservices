package ro.facultate.pos.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.facultate.pos.client.SalesClient;
import ro.facultate.pos.dto.VanzatorResponse;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.repository.UtilizatorRepository;

import java.util.Map;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UtilizatorRepository utilizatorRepository;
    private final SalesClient salesClient;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UtilizatorRepository utilizatorRepository, SalesClient salesClient,
                        PasswordEncoder passwordEncoder) {
        this.utilizatorRepository = utilizatorRepository;
        this.salesClient = salesClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (utilizatorRepository.count() > 0) {
            return;
        }

        VanzatorResponse vanzator;
        try {
            vanzator = salesClient.creeazaVanzator(Map.of("nume", "Administrator"));
        } catch (Exception e) {
            log.warn("Nu s-a putut crea contul ADMIN implicit - sales-service indisponibil la pornire: {}", e.getMessage());
            return;
        }

        Utilizator admin = new Utilizator();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRol(RolUtilizator.ADMIN);
        admin.setActiv(true);
        admin.setVanzatorId(vanzator.getId());
        utilizatorRepository.save(admin);

        log.info("Cont ADMIN implicit creat (username 'admin') - schimbati parola dupa prima autentificare");
    }
}
