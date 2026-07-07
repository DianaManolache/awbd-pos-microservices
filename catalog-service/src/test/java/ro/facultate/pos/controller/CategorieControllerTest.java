package ro.facultate.pos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateCategorieRequest;
import ro.facultate.pos.dto.UpdateCategorieRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.service.CategorieService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CategorieController.class)
class CategorieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategorieService categorieService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCategorie_success_returns201() throws Exception {
        CreateCategorieRequest req = new CreateCategorieRequest();
        req.setNume("Panificatie");

        Categorie saved = new Categorie();
        saved.setId(1L);
        saved.setNume("Panificatie");

        Mockito.when(categorieService.create(Mockito.any(CreateCategorieRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/categorii")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nume").value("Panificatie"));
    }

    @Test
    void createCategorie_invalid_returns400() throws Exception {
        String body = "{\"nume\":\"\"}";

        mockMvc.perform(post("/api/categorii")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_success_returns200() throws Exception {
        Categorie c1 = new Categorie();
        c1.setId(1L);
        c1.setNume("Panificatie");

        Mockito.when(categorieService.getAll()).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/categorii"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nume").value("Panificatie"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        Mockito.when(categorieService.getById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));

        mockMvc.perform(get("/api/categorii/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_success_returns200() throws Exception {
        UpdateCategorieRequest req = new UpdateCategorieRequest();
        req.setNume("Panificatie si patiserie");

        Categorie updated = new Categorie();
        updated.setId(1L);
        updated.setNume("Panificatie si patiserie");

        Mockito.when(categorieService.update(Mockito.eq(1L), Mockito.any(UpdateCategorieRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/categorii/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nume").value("Panificatie si patiserie"));
    }

    @Test
    void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/categorii/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(categorieService).delete(1L);
    }

    @Test
    void delete_withProduse_returns400() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria are produse asociate"))
                .when(categorieService).delete(1L);

        mockMvc.perform(delete("/api/categorii/1"))
                .andExpect(status().isBadRequest());
    }
}
