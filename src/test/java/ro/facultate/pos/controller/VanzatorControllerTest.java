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
import ro.facultate.pos.dto.CreateVanzatorRequest;
import ro.facultate.pos.dto.UpdateVanzatorRequest;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.service.VanzatorService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(VanzatorController.class)
class VanzatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VanzatorService vanzatorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createVanzator_success_returns201() throws Exception {
        CreateVanzatorRequest req = new CreateVanzatorRequest();
        req.setNume("Vanzator 1");

        Vanzator saved = new Vanzator();
        saved.setId(1L);
        saved.setNume("Vanzator 1");

        Mockito.when(vanzatorService.create(Mockito.any(CreateVanzatorRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/vanzatori")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nume").value("Vanzator 1"));
    }

    @Test
    void createVanzator_blankName_returns400() throws Exception {
        String body = "{\"nume\":\"\"}";

        mockMvc.perform(post("/api/vanzatori")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_success_returns200() throws Exception {
        Vanzator v1 = new Vanzator();
        v1.setId(1L);
        v1.setNume("Vanzator 1");

        Mockito.when(vanzatorService.getAll()).thenReturn(List.of(v1));

        mockMvc.perform(get("/api/vanzatori"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nume").value("Vanzator 1"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        Mockito.when(vanzatorService.getById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Vanzator not found"));

        mockMvc.perform(get("/api/vanzatori/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_success_returns200() throws Exception {
        UpdateVanzatorRequest req = new UpdateVanzatorRequest();
        req.setNume("Vanzator Principal");

        Vanzator updated = new Vanzator();
        updated.setId(1L);
        updated.setNume("Vanzator Principal");

        Mockito.when(vanzatorService.update(Mockito.eq(1L), Mockito.any(UpdateVanzatorRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/vanzatori/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nume").value("Vanzator Principal"));
    }

    @Test
    void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/vanzatori/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(vanzatorService).delete(1L);
    }

    @Test
    void delete_withBonuri_returns400() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are bonuri asociate"))
                .when(vanzatorService).delete(1L);

        mockMvc.perform(delete("/api/vanzatori/1"))
                .andExpect(status().isBadRequest());
    }
}
