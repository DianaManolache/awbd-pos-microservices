package ro.facultate.pos.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.repository.UtilizatorRepository;
import ro.facultate.pos.repository.VanzatorRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UtilizatorRepository utilizatorRepository;
    private final VanzatorRepository vanzatorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(UtilizatorRepository utilizatorRepository, VanzatorRepository vanzatorRepository,
                        PasswordEncoder passwordEncoder) {
        this.utilizatorRepository = utilizatorRepository;
        this.vanzatorRepository = vanzatorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (utilizatorRepository.count() > 0) {
            return;
        }

        Vanzator vanzator = new Vanzator();
        vanzator.setNume("Administrator");
        vanzator = vanzatorRepository.save(vanzator);

        Utilizator admin = new Utilizator();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setRol(RolUtilizator.ADMIN);
        admin.setActiv(true);
        admin.setVanzator(vanzator);
        utilizatorRepository.save(admin);

        log.info("Cont ADMIN implicit creat (username 'admin') - schimbati parola dupa prima autentificare");
    }
}
