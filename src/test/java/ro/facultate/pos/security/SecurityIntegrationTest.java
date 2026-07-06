package ro.facultate.pos.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.repository.UtilizatorRepository;
import ro.facultate.pos.repository.VanzatorRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtilizatorRepository utilizatorRepository;

    @Autowired
    private VanzatorRepository vanzatorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void seedUser(String username, String rawPassword, RolUtilizator rol) {
        Vanzator vanzator = new Vanzator();
        vanzator.setNume("Vanzator " + username);
        vanzator = vanzatorRepository.save(vanzator);

        Utilizator u = new Utilizator();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRol(rol);
        u.setActiv(true);
        u.setVanzator(vanzator);
        utilizatorRepository.save(u);
    }

    @Test
    void anonymousBrowserRequest_toAdminPage_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/web/categorii").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void anonymousBrowserRequest_toBonuri_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/web/bonuri").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void anonymousApiRequest_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/categorii").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRole_canAccessBonuri() throws Exception {
        mockMvc.perform(get("/web/bonuri"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userRole_isForbiddenFromAdminPages() throws Exception {
        mockMvc.perform(get("/web/categorii"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_canAccessAdminPages() throws Exception {
        mockMvc.perform(get("/web/categorii"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_canAccessBonuri() throws Exception {
        mockMvc.perform(get("/web/bonuri"))
                .andExpect(status().isOk());
    }

    @Test
    void login_withValidCredentials_authenticatesSuccessfully() throws Exception {
        seedUser("cashier1", "parola123", RolUtilizator.USER);

        mockMvc.perform(formLogin().user("cashier1").password("parola123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("/login?error"))));
    }

    @Test
    void login_withWrongPassword_redirectsToLoginWithError() throws Exception {
        seedUser("cashier2", "parola123", RolUtilizator.USER);

        mockMvc.perform(formLogin().user("cashier2").password("gresita"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login?error")));
    }

    @Test
    void login_withUnknownUsername_redirectsToLoginWithError() throws Exception {
        mockMvc.perform(formLogin().user("nu.exista").password("orice"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login?error")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void logout_redirectsToLoginWithLogoutParam() throws Exception {
        mockMvc.perform(logout())
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/login?logout")));
    }
}
