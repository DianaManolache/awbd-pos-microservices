package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.service.UtilizatorService;
import ro.facultate.pos.service.VanzatorService;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilizatorViewController.class)
class UtilizatorViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UtilizatorService utilizatorService;

    @MockBean
    private VanzatorService vanzatorService;

    @Test
    void list_rendersListView() throws Exception {
        Utilizator u = new Utilizator();
        u.setId(1L);
        u.setUsername("ana");
        u.setRol(RolUtilizator.USER);
        u.setActiv(true);
        Mockito.when(utilizatorService.getAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/web/utilizatori"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilizatori/list"));
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        Mockito.when(vanzatorService.getAll()).thenReturn(List.of(new Vanzator()));

        mockMvc.perform(get("/web/utilizatori/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilizatori/form"));
    }

    @Test
    void create_invalid_rendersFormWithoutRedirect() throws Exception {
        Mockito.when(vanzatorService.getAll()).thenReturn(List.of(new Vanzator()));

        mockMvc.perform(post("/web/utilizatori")
                        .param("username", "")
                        .param("password", "parola123")
                        .param("rol", "USER")
                        .param("vanzatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilizatori/form"));

        Mockito.verify(utilizatorService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/utilizatori")
                        .param("username", "ana.vanzatoare")
                        .param("password", "parola123")
                        .param("rol", "USER")
                        .param("vanzatorId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/utilizatori"));
    }

    @Test
    void delete_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilizatorul nu exista"))
                .when(utilizatorService).delete(1L);

        mockMvc.perform(post("/web/utilizatori/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/utilizatori"))
                .andExpect(flash().attribute("businessError", "Utilizatorul nu exista"));
    }

    @Test
    void editForm_prefillsFormFromExistingUtilizator() throws Exception {
        Utilizator u = new Utilizator();
        u.setId(1L);
        u.setUsername("ana");
        u.setRol(RolUtilizator.ADMIN);
        u.setActiv(true);
        Mockito.when(utilizatorService.getById(1L)).thenReturn(u);

        mockMvc.perform(get("/web/utilizatori/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilizatori/form"))
                .andExpect(model().attribute("utilizator", hasProperty("username", equalTo("ana"))))
                .andExpect(model().attribute("utilizator", hasProperty("rol", equalTo(RolUtilizator.ADMIN))))
                .andExpect(model().attribute("utilizator", hasProperty("activ", equalTo(true))));
    }

    @Test
    void update_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/utilizatori/1")
                        .param("username", "")
                        .param("password", "parola123")
                        .param("rol", "USER")
                        .param("activ", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilizatori/form"));

        Mockito.verify(utilizatorService, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void update_businessError_rendersFormWithBusinessError() throws Exception {
        Mockito.when(utilizatorService.update(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username deja folosit"));

        mockMvc.perform(post("/web/utilizatori/1")
                        .param("username", "ana")
                        .param("password", "parola123")
                        .param("rol", "USER")
                        .param("activ", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("utilizatori/form"))
                .andExpect(model().attribute("businessError", "Username deja folosit"));
    }

    @Test
    void update_valid_redirectsToList() throws Exception {
        Utilizator u = new Utilizator();
        u.setId(1L);
        u.setUsername("ana");
        u.setRol(RolUtilizator.USER);
        u.setActiv(true);
        Mockito.when(utilizatorService.update(Mockito.eq(1L), Mockito.any())).thenReturn(u);

        mockMvc.perform(post("/web/utilizatori/1")
                        .param("username", "ana")
                        .param("password", "parola123")
                        .param("rol", "USER")
                        .param("activ", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/utilizatori"));
    }

    @Test
    void delete_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/utilizatori/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/utilizatori"));

        Mockito.verify(utilizatorService).delete(1L);
    }
}
