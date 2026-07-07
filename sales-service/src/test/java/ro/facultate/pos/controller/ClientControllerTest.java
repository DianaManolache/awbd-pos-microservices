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
import ro.facultate.pos.dto.CreateClientRequest;
import ro.facultate.pos.dto.UpdateClientRequest;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.service.ClientService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ClientController.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createClient_success_returns201() throws Exception {
        CreateClientRequest req = new CreateClientRequest();
        req.setNume("Maria Ionescu");
        req.setEmail("maria@example.com");
        req.setTelefon("0722000000");

        Client saved = new Client();
        saved.setId(1L);
        saved.setNume("Maria Ionescu");
        saved.setEmail("maria@example.com");
        saved.setTelefon("0722000000");

        Mockito.when(clientService.create(Mockito.any(CreateClientRequest.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nume").value("Maria Ionescu"))
                .andExpect(jsonPath("$.email").value("maria@example.com"))
                .andExpect(jsonPath("$.telefon").value("0722000000"));
    }

    @Test
    void createClient_invalidEmail_returns400() throws Exception {
        String body = """
                {
                  "nume": "Maria Ionescu",
                  "email": "maria.com",
                  "telefon": "0722000000"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createClient_blankName_returns400() throws Exception {
        String body = """
                {
                  "nume": "",
                  "email": "maria@example.com",
                  "telefon": "0722000000"
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_success_returns200() throws Exception {
        Client c1 = new Client();
        c1.setId(1L);
        c1.setNume("Maria");

        Mockito.when(clientService.getAll()).thenReturn(List.of(c1));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nume").value("Maria"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        Mockito.when(clientService.getById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        mockMvc.perform(get("/api/clients/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_success_returns200() throws Exception {
        UpdateClientRequest req = new UpdateClientRequest();
        req.setNume("Maria Popescu");
        req.setEmail("maria.popescu@example.com");
        req.setTelefon("0733000000");

        Client updated = new Client();
        updated.setId(1L);
        updated.setNume("Maria Popescu");
        updated.setEmail("maria.popescu@example.com");
        updated.setTelefon("0733000000");

        Mockito.when(clientService.update(Mockito.eq(1L), Mockito.any(UpdateClientRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/clients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nume").value("Maria Popescu"));
    }

    @Test
    void delete_success_returns204() throws Exception {
        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(clientService).delete(1L);
    }

    @Test
    void delete_withBonuri_returns400() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clientul are bonuri asociate"))
                .when(clientService).delete(1L);

        mockMvc.perform(delete("/api/clients/1"))
                .andExpect(status().isBadRequest());
    }
}
