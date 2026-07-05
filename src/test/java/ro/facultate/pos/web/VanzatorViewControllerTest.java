package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.service.VanzatorService;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VanzatorViewController.class)
class VanzatorViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VanzatorService vanzatorService;

    @Test
    void list_rendersListView() throws Exception {
        Vanzator v = new Vanzator();
        v.setId(1L);
        v.setNume("Vanzator 1");
        Mockito.when(vanzatorService.getAll()).thenReturn(List.of(v));

        mockMvc.perform(get("/web/vanzatori"))
                .andExpect(status().isOk())
                .andExpect(view().name("vanzatori/list"));
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        mockMvc.perform(get("/web/vanzatori/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("vanzatori/form"));
    }

    @Test
    void create_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/vanzatori").param("nume", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("vanzatori/form"));

        Mockito.verify(vanzatorService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/vanzatori").param("nume", "Vanzator 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/vanzatori"));
    }

    @Test
    void delete_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanzatorul are bonuri asociate"))
                .when(vanzatorService).delete(1L);

        mockMvc.perform(post("/web/vanzatori/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/vanzatori"))
                .andExpect(flash().attribute("businessError", "Vanzatorul are bonuri asociate"));
    }

    @Test
    void delete_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/vanzatori/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/vanzatori"));

        Mockito.verify(vanzatorService).delete(1L);
    }

    @Test
    void editForm_prefillsFormFromExistingVanzator() throws Exception {
        Vanzator v = new Vanzator();
        v.setId(1L);
        v.setNume("Vanzator 1");
        Mockito.when(vanzatorService.getById(1L)).thenReturn(v);

        mockMvc.perform(get("/web/vanzatori/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("vanzatori/form"))
                .andExpect(model().attribute("vanzator", hasProperty("nume", equalTo("Vanzator 1"))));
    }

    @Test
    void update_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/vanzatori/1").param("nume", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("vanzatori/form"));

        Mockito.verify(vanzatorService, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void update_businessError_rendersFormWithBusinessError() throws Exception {
        Mockito.when(vanzatorService.update(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "some message"));

        mockMvc.perform(post("/web/vanzatori/1").param("nume", "Vanzator 1"))
                .andExpect(status().isOk())
                .andExpect(view().name("vanzatori/form"))
                .andExpect(model().attribute("businessError", "some message"));
    }

    @Test
    void update_valid_redirectsToList() throws Exception {
        Vanzator v = new Vanzator();
        v.setId(1L);
        v.setNume("Vanzator 1");
        Mockito.when(vanzatorService.update(Mockito.eq(1L), Mockito.any())).thenReturn(v);

        mockMvc.perform(post("/web/vanzatori/1").param("nume", "Vanzator 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/vanzatori"));
    }
}
