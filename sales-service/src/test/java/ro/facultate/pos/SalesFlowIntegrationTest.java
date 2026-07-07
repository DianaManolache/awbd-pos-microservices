package ro.facultate.pos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ro.facultate.pos.client.CatalogClient;
import ro.facultate.pos.dto.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SalesFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CatalogClient catalogClient;

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
        Long clientId = createClient("Maria Ionescu");
        Long vanzatorId = createVanzator("Vanzator 1");
        Long bonId = createBon(clientId, vanzatorId);

        ProdusResponse produs = new ProdusResponse();
        produs.setId(10L);
        produs.setNume("Suc portocale");
        produs.setPret(BigDecimal.valueOf(5.0));
        produs.setStoc(10);
        Mockito.when(catalogClient.getProdus(10L)).thenReturn(produs);

        AddBonProdusRequest addReq = new AddBonProdusRequest();
        addReq.setProdusId(10L);
        addReq.setCantitate(3);

        mockMvc.perform(post("/api/bons/" + bonId + "/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isCreated());

        Mockito.verify(catalogClient).ajusteazaStoc(10L, Map.of("delta", -3));

        PayBonRequest payReq = new PayBonRequest();
        payReq.setTipPlata(ro.facultate.pos.entity.enums.TipPlata.CARD);

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
    }

    @Test
    void stocInsuficient_endToEnd() throws Exception {
        Long clientId = createClient("Ion Popescu");
        Long vanzatorId = createVanzator("Vanzator 2");
        Long bonId = createBon(clientId, vanzatorId);

        ProdusResponse produs = new ProdusResponse();
        produs.setId(20L);
        produs.setNume("Paine");
        produs.setPret(BigDecimal.valueOf(3.0));
        produs.setStoc(2);
        Mockito.when(catalogClient.getProdus(20L)).thenReturn(produs);

        feign.Request request = feign.Request.create(
                feign.Request.HttpMethod.POST, "/api/produse/20/ajusteaza-stoc",
                Map.of(), null, StandardCharsets.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(400).reason("Bad Request").request(request).headers(Map.of()).build();
        Mockito.doThrow(feign.FeignException.errorStatus("CatalogClient#ajusteazaStoc", response))
                .when(catalogClient).ajusteazaStoc(20L, Map.of("delta", -5));

        AddBonProdusRequest addReq = new AddBonProdusRequest();
        addReq.setProdusId(20L);
        addReq.setCantitate(5);

        mockMvc.perform(post("/api/bons/" + bonId + "/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addReq)))
                .andExpect(status().isBadRequest());
    }
}
