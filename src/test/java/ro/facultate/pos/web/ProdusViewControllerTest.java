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
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.service.CategorieService;
import ro.facultate.pos.service.ProdusService;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProdusViewController.class)
class ProdusViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdusService produsService;

    @MockBean
    private CategorieService categorieService;

    @Test
    void list_rendersListView() throws Exception {
        Produs p = new Produs();
        p.setId(1L);
        p.setNume("Paine");
        p.setPret(BigDecimal.valueOf(3.5));
        p.setStoc(10);
        Mockito.when(produsService.getAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/web/produse"))
                .andExpect(status().isOk())
                .andExpect(view().name("produse/list"));
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        Mockito.when(categorieService.getAll()).thenReturn(List.of(new Categorie()));

        mockMvc.perform(get("/web/produse/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("produse/form"));
    }

    @Test
    void create_invalid_rendersFormWithoutRedirect() throws Exception {
        Mockito.when(categorieService.getAll()).thenReturn(List.of(new Categorie()));

        mockMvc.perform(post("/web/produse")
                        .param("nume", "Paine")
                        .param("pret", "-5")
                        .param("stoc", "10")
                        .param("categorieId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("produse/form"));

        Mockito.verify(produsService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/produse")
                        .param("nume", "Paine")
                        .param("pret", "3.5")
                        .param("stoc", "10")
                        .param("categorieId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/produse"));
    }

    @Test
    void delete_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este pe cel putin un bon"))
                .when(produsService).delete(1L);

        mockMvc.perform(post("/web/produse/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/produse"))
                .andExpect(flash().attribute("businessError", "Produsul este pe cel putin un bon"));
    }

    @Test
    void delete_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/produse/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/produse"));

        Mockito.verify(produsService).delete(1L);
    }

    @Test
    void editForm_prefillsFormFromExistingProdus() throws Exception {
        Categorie categorie = new Categorie();
        categorie.setId(2L);
        categorie.setNume("Panificatie");

        Produs p = new Produs();
        p.setId(1L);
        p.setNume("Paine");
        p.setPret(BigDecimal.valueOf(3.5));
        p.setStoc(10);
        p.setCategorie(categorie);

        Mockito.when(produsService.getById(1L)).thenReturn(p);
        Mockito.when(categorieService.getAll()).thenReturn(List.of(categorie));

        mockMvc.perform(get("/web/produse/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("produse/form"))
                .andExpect(model().attribute("produs", allOf(
                        hasProperty("nume", equalTo("Paine")),
                        hasProperty("pret", equalTo(BigDecimal.valueOf(3.5))),
                        hasProperty("stoc", equalTo(10)),
                        hasProperty("categorieId", equalTo(2L))
                )));
    }

    @Test
    void update_invalid_rendersFormWithoutRedirect() throws Exception {
        Mockito.when(categorieService.getAll()).thenReturn(List.of(new Categorie()));

        mockMvc.perform(post("/web/produse/1")
                        .param("nume", "")
                        .param("pret", "3.5")
                        .param("stoc", "10")
                        .param("categorieId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("produse/form"));

        Mockito.verify(produsService, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void update_businessError_rendersFormWithBusinessError() throws Exception {
        Mockito.when(produsService.update(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "some message"));

        mockMvc.perform(post("/web/produse/1")
                        .param("nume", "Paine")
                        .param("pret", "3.5")
                        .param("stoc", "10")
                        .param("categorieId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("produse/form"))
                .andExpect(model().attribute("businessError", "some message"));
    }

    @Test
    void update_valid_redirectsToList() throws Exception {
        Produs p = new Produs();
        p.setId(1L);
        p.setNume("Paine");
        p.setPret(BigDecimal.valueOf(3.5));
        p.setStoc(10);
        Mockito.when(produsService.update(Mockito.eq(1L), Mockito.any())).thenReturn(p);

        mockMvc.perform(post("/web/produse/1")
                        .param("nume", "Paine")
                        .param("pret", "3.5")
                        .param("stoc", "10")
                        .param("categorieId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/produse"));
    }
}
