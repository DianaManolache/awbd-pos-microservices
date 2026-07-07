package ro.facultate.pos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.AjusteazaStocRequest;
import ro.facultate.pos.dto.CreateProdusRequest;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.dto.UpdateStocRequest;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.service.ProdusService;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ProdusController.class)
class ProdusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdusService produsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createProdus_success_returns201() throws Exception {
        CreateProdusRequest req = new CreateProdusRequest();
        req.setNume("Coca Cola");
        req.setPret(BigDecimal.valueOf(5.5));
        req.setStoc(70);
        req.setCategorieId(2L);

        Produs saved = new Produs();
        saved.setId(1L);
        saved.setNume("Coca Cola");
        saved.setPret(BigDecimal.valueOf(5.5));
        saved.setStoc(70);

        Mockito.when(produsService.create(Mockito.any(CreateProdusRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nume").value("Coca Cola"))
                .andExpect(jsonPath("$.pret").value(5.5))
                .andExpect(jsonPath("$.stoc").value(70));
    }

    @Test
    void createProdus_invalidPrice_returns400() throws Exception {
        String body = """
                {
                  "nume": "Coca Cola",
                  "pret": -5.5,
                  "stoc": 70,
                  "categorieId": 2
                }
                """;

        mockMvc.perform(post("/api/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProdus_invalidStock_returns400() throws Exception {
        String body = """
                {
                  "nume": "Coca Cola",
                  "pret": 5.5,
                  "stoc": -1,
                  "categorieId": 2
                }
                """;

        mockMvc.perform(post("/api/produse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_success_returns200() throws Exception {
        Produs p1 = new Produs();
        p1.setId(1L);
        p1.setNume("Coca Cola");
        p1.setPret(BigDecimal.valueOf(5.5));
        p1.setStoc(70);

        Mockito.when(produsService.getAll()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/produse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nume").value("Coca Cola"));
    }

    @Test
    void getByCategorie_success_returns200() throws Exception {
        Produs p1 = new Produs();
        p1.setId(1L);
        p1.setNume("Coca Cola");
        p1.setPret(BigDecimal.valueOf(5.5));
        p1.setStoc(70);

        Mockito.when(produsService.getByCategorie(2L)).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/produse/categorie/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nume").value("Coca Cola"));
    }

    @Test
    void updateStoc_success_returns200() throws Exception {
        UpdateStocRequest req = new UpdateStocRequest();
        req.setStoc(100);

        Produs updated = new Produs();
        updated.setId(1L);
        updated.setNume("Coca Cola");
        updated.setPret(BigDecimal.valueOf(5.5));
        updated.setStoc(100);

        Mockito.when(produsService.updateStoc(Mockito.eq(1L), Mockito.any(UpdateStocRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/produse/1/stoc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stoc").value(100));
    }

    @Test
    void updateStoc_negative_returns400() throws Exception {
        String body = """
                { "stoc": -10 }
                """;

        mockMvc.perform(put("/api/produse/1/stoc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ajusteazaStoc_success_returns200() throws Exception {
        Produs updated = new Produs();
        updated.setId(1L);
        updated.setStoc(7);
        Mockito.when(produsService.ajusteazaStoc(Mockito.eq(1L), Mockito.any())).thenReturn(updated);

        AjusteazaStocRequest req = new AjusteazaStocRequest();
        req.setDelta(-3);

        mockMvc.perform(post("/api/produse/1/ajusteaza-stoc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stoc").value(7));
    }

    @Test
    void ajusteazaStoc_stocInsuficient_returns400() throws Exception {
        Mockito.when(produsService.ajusteazaStoc(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stoc insuficient"));

        AjusteazaStocRequest req = new AjusteazaStocRequest();
        req.setDelta(-100);

        mockMvc.perform(post("/api/produse/1/ajusteaza-stoc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        Mockito.when(produsService.getById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        mockMvc.perform(get("/api/produse/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_success_returns200() throws Exception {
        UpdateProdusRequest req = new UpdateProdusRequest();
        req.setNume("Coca Cola Zero");
        req.setPret(BigDecimal.valueOf(6.0));
        req.setStoc(50);
        req.setCategorieId(2L);

        Produs updated = new Produs();
        updated.setId(1L);
        updated.setNume("Coca Cola Zero");
        updated.setPret(BigDecimal.valueOf(6.0));
        updated.setStoc(50);

        Mockito.when(produsService.update(Mockito.eq(1L), Mockito.any(UpdateProdusRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/produse/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nume").value("Coca Cola Zero"));
    }

    @Test
    void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/produse/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(produsService).delete(1L);
    }

    @Test
    void delete_referenced_returns400() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este pe cel putin un bon"))
                .when(produsService).delete(1L);

        mockMvc.perform(delete("/api/produse/1"))
                .andExpect(status().isBadRequest());
    }
}
