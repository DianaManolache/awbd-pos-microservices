package ro.facultate.pos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.facultate.pos.dto.*;
import ro.facultate.pos.entity.enums.TipPlata;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(roles = {"ADMIN"})
class SalesFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private ProdusRepository produsRepository;

    private Long createCategorie(String nume) throws Exception {
        CreateCategorieRequest req = new CreateCategorieRequest();
        req.setNume(nume);

        String body = mockMvc.perform(post("/api/categorii")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createProdus(String nume, double pret, int stoc, Long categorieId) throws Exception {
        CreateProdusRequest req = new CreateProdusRequest();
        req.setNume(nume);
        req.setPret(BigDecimal.valueOf(pret));
        req.setStoc(stoc);
        req.setCategorieId(categorieId);

        String body = mockMvc.perform(post("/api/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createClient(String nume) throws Exception {
        CreateClientRequest req = new CreateClientRequest();
        req.setNume(nume);

        String body = mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createVanzator(String nume) throws Exception {
        CreateVanzatorRequest req = new CreateVanzatorRequest();
        req.setNume(nume);

        String body = mockMvc.perform(post("/api/vanzatori")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createBon(Long clientId, Long vanzatorId) throws Exception {
        CreateBonRequest req = new CreateBonRequest();
        req.setClientId(clientId);
        req.setVanzatorId(vanzatorId);

        String body = mockMvc.perform(post("/api/bons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void fluxCompletDeVanzare_endToEnd() throws Exception {
        Long categorieId = createCategorie("Bauturi");
        Long produsId = createProdus("Suc portocale", 5.0, 10, categorieId);
        Long clientId = createClient("Maria Ionescu");
        Long vanzatorId = createVanzator("Vanzator 1");
        Long bonId = createBon(clientId, vanzatorId);

        AddBonProdusRequest addReq = new AddBonProdusRequest();
        addReq.setProdusId(produsId);
        addReq.setCantitate(3);

        mockMvc.perform(post("/api/bons/" + bonId + "/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isCreated());

        // stocul a scazut in baza de date reala
        assertEquals(7, produsRepository.findById(produsId).orElseThrow().getStoc());

        PayBonRequest payReq = new PayBonRequest();
        payReq.setTipPlata(TipPlata.CARD);

        mockMvc.perform(post("/api/bons/" + bonId + "/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tip").value("CARD"))
                .andExpect(jsonPath("$.suma").value(15.0))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/api/bons/" + bonId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.total").value(15.0));

        mockMvc.perform(get("/api/bons/" + bonId + "/plati"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tip").value("CARD"));
    }

    @Test
    void stocInsuficient_endToEnd() throws Exception {
        Long categorieId = createCategorie("Panificatie");
        Long produsId = createProdus("Paine", 3.0, 2, categorieId);
        Long clientId = createClient("Ion Popescu");
        Long vanzatorId = createVanzator("Vanzator 2");
        Long bonId = createBon(clientId, vanzatorId);

        AddBonProdusRequest addReq = new AddBonProdusRequest();
        addReq.setProdusId(produsId);
        addReq.setCantitate(5);

        mockMvc.perform(post("/api/bons/" + bonId + "/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isBadRequest());

        // stocul nu s-a modificat in baza de date reala
        assertEquals(2, produsRepository.findById(produsId).orElseThrow().getStoc());
    }

    @Test
    void stergereCategorieCuProduse_blocataEndToEnd() throws Exception {
        Long categorieId = createCategorie("Lactate");
        createProdus("Lapte", 4.0, 20, categorieId);

        mockMvc.perform(delete("/api/categorii/" + categorieId))
                .andExpect(status().isBadRequest());

        // categoria si produsul inca exista in baza de date reala
        assertTrue(categorieRepository.findById(categorieId).isPresent());
    }
}
