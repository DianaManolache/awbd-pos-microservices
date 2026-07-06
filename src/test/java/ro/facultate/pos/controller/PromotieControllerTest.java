package ro.facultate.pos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreatePromotieRequest;
import ro.facultate.pos.entity.Promotie;
import ro.facultate.pos.service.PromotieService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(PromotieController.class)
class PromotieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotieService promotieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_success_returns201() throws Exception {
        CreatePromotieRequest req = new CreatePromotieRequest();
        req.setNume("Reduceri de vara");
        req.setProcentReducere(BigDecimal.valueOf(15));
        req.setDataStart(LocalDateTime.of(2026, 7, 1, 0, 0));
        req.setDataFinal(LocalDateTime.of(2026, 7, 31, 0, 0));

        Promotie saved = new Promotie();
        saved.setId(1L);
        saved.setNume("Reduceri de vara");

        Mockito.when(promotieService.create(Mockito.any(CreatePromotieRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/promotii")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nume").value("Reduceri de vara"));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        String body = "{\"nume\":\"\"}";

        mockMvc.perform(post("/api/promotii")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_success_returns200() throws Exception {
        Promotie p1 = new Promotie();
        p1.setId(1L);
        p1.setNume("Reduceri de vara");

        Mockito.when(promotieService.getAll()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/promotii"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nume").value("Reduceri de vara"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        Mockito.when(promotieService.getById(99L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Promotie not found"));

        mockMvc.perform(get("/api/promotii/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/promotii/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(promotieService).delete(1L);
    }

    @Test
    void addProdus_success_returns200() throws Exception {
        Promotie updated = new Promotie();
        updated.setId(1L);
        updated.setNume("Reduceri de vara");

        Mockito.when(promotieService.addProdus(1L, 2L)).thenReturn(updated);

        mockMvc.perform(post("/api/promotii/1/produse/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void removeProdus_success_returns200() throws Exception {
        Promotie updated = new Promotie();
        updated.setId(1L);
        updated.setNume("Reduceri de vara");

        Mockito.when(promotieService.removeProdus(1L, 2L)).thenReturn(updated);

        mockMvc.perform(delete("/api/promotii/1/produse/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}
