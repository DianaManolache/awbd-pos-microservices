package ro.facultate.pos.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.repository.UtilizatorRepository;

import java.util.List;

@Service
public class UtilizatorDetailsService implements UserDetailsService {

    private final UtilizatorRepository utilizatorRepository;

    public UtilizatorDetailsService(UtilizatorRepository utilizatorRepository) {
        this.utilizatorRepository = utilizatorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilizator u = utilizatorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizator inexistent: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(u.getActiv()))
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())))
                .build();
    }
}
