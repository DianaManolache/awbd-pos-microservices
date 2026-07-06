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
import ro.facultate.pos.dto.CreateUtilizatorRequest;
import ro.facultate.pos.dto.UpdateUtilizatorRequest;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.service.UtilizatorService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UtilizatorController.class)
class UtilizatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UtilizatorService utilizatorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_success_returns201() throws Exception {
        CreateUtilizatorRequest req = new CreateUtilizatorRequest();
        req.setUsername("ana.vanzatoare");
        req.setPassword("parola123");
        req.setRol(RolUtilizator.USER);
        req.setVanzatorId(1L);

        Utilizator saved = new Utilizator();
        saved.setId(1L);
        saved.setUsername("ana.vanzatoare");
        saved.setRol(RolUtilizator.USER);
        saved.setActiv(true);

        Mockito.when(utilizatorService.create(Mockito.any(CreateUtilizatorRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/utilizatori")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ana.vanzatoare"));
    }

    @Test
    void create_invalid_returns400() throws Exception {
        String body = "{\"username\":\"\"}";

        mockMvc.perform(post("/api/utilizatori")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_success_returns200() throws Exception {
        Utilizator u1 = new Utilizator();
        u1.setId(1L);
        u1.setUsername("ana.vanzatoare");

        Mockito.when(utilizatorService.getAll()).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/utilizatori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("ana.vanzatoare"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        Mockito.when(utilizatorService.getById(99L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Utilizator not found"));

        mockMvc.perform(get("/api/utilizatori/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_success_returns200() throws Exception {
        UpdateUtilizatorRequest req = new UpdateUtilizatorRequest();
        req.setUsername("ana.noua");
        req.setPassword("parolaNoua123");
        req.setRol(RolUtilizator.ADMIN);
        req.setActiv(false);

        Utilizator updated = new Utilizator();
        updated.setId(1L);
        updated.setUsername("ana.noua");
        updated.setRol(RolUtilizator.ADMIN);
        updated.setActiv(false);

        Mockito.when(utilizatorService.update(Mockito.eq(1L), Mockito.any(UpdateUtilizatorRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/utilizatori/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ana.noua"));
    }

    @Test
    void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/utilizatori/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(utilizatorService).delete(1L);
    }
}
