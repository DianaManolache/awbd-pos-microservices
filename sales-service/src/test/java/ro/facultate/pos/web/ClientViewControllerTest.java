package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.service.ClientService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ClientViewController.class)
class ClientViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Test
    void list_rendersListView() throws Exception {
        Client c = new Client();
        c.setId(1L);
        c.setNume("Maria");
        Mockito.when(clientService.getPage(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(c)));

        mockMvc.perform(get("/web/clienti"))
                .andExpect(status().isOk())
                .andExpect(view().name("clienti/list"));
    }

    @Test
    void list_defaultSort_usesNumeAscending() throws Exception {
        Mockito.when(clientService.getPage(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/web/clienti"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(clientService).getPage(captor.capture());
        Pageable used = captor.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(5, used.getPageSize());
        assertEquals(Sort.Direction.ASC, used.getSort().getOrderFor("nume").getDirection());
    }

    @Test
    void list_customSortAndSize_buildsPageableCorrectly() throws Exception {
        Mockito.when(clientService.getPage(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/web/clienti").param("page", "1").param("size", "10").param("sort", "email").param("dir", "desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(clientService).getPage(captor.capture());
        Pageable used = captor.getValue();
        assertEquals(1, used.getPageNumber());
        assertEquals(10, used.getPageSize());
        assertEquals(Sort.Direction.DESC, used.getSort().getOrderFor("email").getDirection());
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        mockMvc.perform(get("/web/clienti/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("clienti/form"));
    }

    @Test
    void create_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/clienti").param("nume", "").param("email", "not-an-email"))
                .andExpect(status().isOk())
                .andExpect(view().name("clienti/form"));

        Mockito.verify(clientService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/clienti").param("nume", "Maria Ionescu").param("email", "maria@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clienti"));
    }

    @Test
    void delete_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clientul are bonuri asociate"))
                .when(clientService).delete(1L);

        mockMvc.perform(post("/web/clienti/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clienti"))
                .andExpect(flash().attribute("businessError", "Clientul are bonuri asociate"));
    }

    @Test
    void delete_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/clienti/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clienti"));

        Mockito.verify(clientService).delete(1L);
    }

    @Test
    void editForm_prefillsFormFromExistingClient() throws Exception {
        Client c = new Client();
        c.setId(1L);
        c.setNume("Maria Ionescu");
        c.setEmail("maria@example.com");
        c.setTelefon("0722000000");

        Mockito.when(clientService.getById(1L)).thenReturn(c);

        mockMvc.perform(get("/web/clienti/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("clienti/form"))
                .andExpect(model().attribute("client", allOf(
                        hasProperty("nume", equalTo("Maria Ionescu")),
                        hasProperty("email", equalTo("maria@example.com")),
                        hasProperty("telefon", equalTo("0722000000"))
                )));
    }

    @Test
    void editForm_idNotFound_redirectsToListWithFlashMessage() throws Exception {
        Mockito.when(clientService.getById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Clientul nu exista"));

        mockMvc.perform(get("/web/clienti/99/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clienti"))
                .andExpect(flash().attribute("businessError", "Clientul nu exista"));
    }

    @Test
    void update_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/clienti/1")
                        .param("nume", "")
                        .param("email", "maria@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("clienti/form"));

        Mockito.verify(clientService, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void update_businessError_rendersFormWithBusinessError() throws Exception {
        Mockito.when(clientService.update(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "some message"));

        mockMvc.perform(post("/web/clienti/1")
                        .param("nume", "Maria Ionescu")
                        .param("email", "maria@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("clienti/form"))
                .andExpect(model().attribute("businessError", "some message"));
    }

    @Test
    void update_valid_redirectsToList() throws Exception {
        Client c = new Client();
        c.setId(1L);
        c.setNume("Maria Ionescu");
        c.setEmail("maria@example.com");
        Mockito.when(clientService.update(Mockito.eq(1L), Mockito.any())).thenReturn(c);

        mockMvc.perform(post("/web/clienti/1")
                        .param("nume", "Maria Ionescu")
                        .param("email", "maria@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/clienti"));
    }
}
