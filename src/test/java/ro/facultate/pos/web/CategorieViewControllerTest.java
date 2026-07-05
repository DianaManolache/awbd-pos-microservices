package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.service.CategorieService;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategorieViewController.class)
class CategorieViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategorieService categorieService;

    @Test
    void list_rendersListView() throws Exception {
        Categorie c = new Categorie();
        c.setId(1L);
        c.setNume("Panificatie");
        Mockito.when(categorieService.getAll()).thenReturn(List.of(c));

        mockMvc.perform(get("/web/categorii"))
                .andExpect(status().isOk())
                .andExpect(view().name("categorii/list"));
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        mockMvc.perform(get("/web/categorii/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("categorii/form"));
    }

    @Test
    void create_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/categorii").param("nume", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categorii/form"));

        Mockito.verify(categorieService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/categorii").param("nume", "Panificatie"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/categorii"));
    }

    @Test
    void delete_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria are produse asociate"))
                .when(categorieService).delete(1L);

        mockMvc.perform(post("/web/categorii/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/categorii"))
                .andExpect(flash().attribute("businessError", "Categoria are produse asociate"));
    }

    @Test
    void delete_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/categorii/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/categorii"));

        Mockito.verify(categorieService).delete(1L);
    }

    @Test
    void editForm_prefillsFormFromExistingCategorie() throws Exception {
        Categorie c = new Categorie();
        c.setId(1L);
        c.setNume("Panificatie");
        Mockito.when(categorieService.getById(1L)).thenReturn(c);

        mockMvc.perform(get("/web/categorii/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("categorii/form"))
                .andExpect(model().attribute("categorie", hasProperty("nume", equalTo("Panificatie"))));
    }

    @Test
    void update_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/categorii/1").param("nume", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categorii/form"));

        Mockito.verify(categorieService, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void update_businessError_rendersFormWithBusinessError() throws Exception {
        Mockito.when(categorieService.update(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "some message"));

        mockMvc.perform(post("/web/categorii/1").param("nume", "Panificatie"))
                .andExpect(status().isOk())
                .andExpect(view().name("categorii/form"))
                .andExpect(model().attribute("businessError", "some message"));
    }

    @Test
    void update_valid_redirectsToList() throws Exception {
        Categorie c = new Categorie();
        c.setId(1L);
        c.setNume("Panificatie");
        Mockito.when(categorieService.update(Mockito.eq(1L), Mockito.any())).thenReturn(c);

        mockMvc.perform(post("/web/categorii/1").param("nume", "Panificatie"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/categorii"));
    }
}
