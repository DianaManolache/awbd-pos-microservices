package ro.facultate.pos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            JwtCookieAuthenticationSuccessHandler jwtSuccessHandler,
                                            JwtService jwtService) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/error",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/web/utilizatori/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(jwtSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies(JwtCookieAuthenticationSuccessHandler.AUTH_COOKIE_NAME)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("pos-remember-me-key")
                        .tokenValiditySeconds(14 * 24 * 60 * 60)
                )
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                // Permite si autentificare stateless prin JWT (folosit de apelurile
                // interne Sales -> User, care nu au sesiunea acestui serviciu) - fara
                // sa afecteze autentificarea normala prin sesiune pentru cererile din
                // browser (filtrul nu face nimic daca deja exista o autentificare).
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
