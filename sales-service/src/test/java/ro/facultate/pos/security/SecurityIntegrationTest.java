package ro.facultate.pos.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ro.facultate.pos.client.CatalogClient;
import ro.facultate.pos.client.UserClient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private CatalogClient catalogClient;

    @MockBean
    private UserClient userClient;

    private String tokenFor(String username, String rol) {
        return jwtService.generateToken(username, rol);
    }

    @Test
    void anonymousRequest_toApi_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/bons"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousRequest_toBonuriPage_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/web/bonuri"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRole_canAccessBonuri() throws Exception {
        mockMvc.perform(get("/web/bonuri")
                        .header("Authorization", "Bearer " + tokenFor("cashier", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void userRole_isForbiddenFromClientiPage() throws Exception {
        mockMvc.perform(get("/web/clienti")
                        .header("Authorization", "Bearer " + tokenFor("cashier", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRole_canAccessClientiPage() throws Exception {
        mockMvc.perform(get("/web/clienti")
                        .header("Authorization", "Bearer " + tokenFor("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void adminRole_canAccessBonuri() throws Exception {
        mockMvc.perform(get("/web/bonuri")
                        .header("Authorization", "Bearer " + tokenFor("admin", "ADMIN")))
                .andExpect(status().isOk());
    }
}
