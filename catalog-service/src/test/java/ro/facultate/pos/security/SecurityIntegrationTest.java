package ro.facultate.pos.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

    private String tokenFor(String username, String rol) {
        return jwtService.generateToken(username, rol);
    }

    @Test
    void anonymousRequest_toApi_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/categorii"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousRequest_toAdminPage_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/web/produse"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validToken_toApi_returnsOk() throws Exception {
        mockMvc.perform(get("/api/categorii")
                        .header("Authorization", "Bearer " + tokenFor("ana", "USER")))
                .andExpect(status().isOk());
    }

    @Test
    void userRole_toAdminPage_returnsForbidden() throws Exception {
        mockMvc.perform(get("/web/produse")
                        .header("Authorization", "Bearer " + tokenFor("ana", "USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRole_toAdminPage_returnsOk() throws Exception {
        mockMvc.perform(get("/web/produse")
                        .header("Authorization", "Bearer " + tokenFor("admin", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void tamperedToken_returnsUnauthorized() throws Exception {
        String token = tokenFor("ana", "ADMIN");
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        mockMvc.perform(get("/api/categorii")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }
}
