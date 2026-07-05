package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.entity.Promotie;
import ro.facultate.pos.service.ProdusService;
import ro.facultate.pos.service.PromotieService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromotieViewController.class)
class PromotieViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotieService promotieService;

    @MockBean
    private ProdusService produsService;

    @Test
    void list_rendersListView() throws Exception {
        Promotie p = new Promotie();
        p.setId(1L);
        p.setNume("Reduceri de vara");
        Mockito.when(promotieService.getAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/web/promotii"))
                .andExpect(status().isOk())
                .andExpect(view().name("promotii/list"));
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        mockMvc.perform(get("/web/promotii/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("promotii/form"));
    }

    @Test
    void create_valid_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/promotii")
                        .param("nume", "Reduceri de vara")
                        .param("procentReducere", "15")
                        .param("dataStart", "2026-07-01T00:00")
                        .param("dataFinal", "2026-07-31T00:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/promotii"));
    }

    @Test
    void create_invalid_rendersFormWithoutRedirect() throws Exception {
        mockMvc.perform(post("/web/promotii")
                        .param("nume", "")
                        .param("procentReducere", "15")
                        .param("dataStart", "2026-07-01T00:00")
                        .param("dataFinal", "2026-07-31T00:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("promotii/form"));

        Mockito.verify(promotieService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void editForm_rendersFormWithProduseAsociate() throws Exception {
        Promotie promotie = new Promotie();
        promotie.setId(1L);
        promotie.setNume("Reduceri de vara");
        promotie.setProcentReducere(BigDecimal.TEN);
        promotie.setDataStart(LocalDateTime.now());
        promotie.setDataFinal(LocalDateTime.now().plusDays(1));
        promotie.setActiva(true);

        Mockito.when(promotieService.getById(1L)).thenReturn(promotie);
        Mockito.when(produsService.getAll()).thenReturn(List.of(new Produs()));

        mockMvc.perform(get("/web/promotii/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("promotii/form"));
    }

    @Test
    void update_invalid_rendersFormWithoutRedirect() throws Exception {
        Promotie existing = new Promotie();
        existing.setId(1L);
        existing.setNume("Reduceri de vara");
        existing.setProcentReducere(BigDecimal.TEN);
        existing.setDataStart(LocalDateTime.now());
        existing.setDataFinal(LocalDateTime.now().plusDays(1));
        existing.setActiva(true);

        Mockito.when(promotieService.getById(1L)).thenReturn(existing);
        Mockito.when(produsService.getAll()).thenReturn(List.of(new Produs()));

        mockMvc.perform(post("/web/promotii/1")
                        .param("nume", "")
                        .param("procentReducere", "15")
                        .param("dataStart", "2026-07-01T00:00")
                        .param("dataFinal", "2026-07-31T00:00")
                        .param("activa", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("promotii/form"));

        Mockito.verify(promotieService, Mockito.never()).update(Mockito.any(), Mockito.any());
    }

    @Test
    void update_businessError_rendersFormWithBusinessError() throws Exception {
        Promotie existing = new Promotie();
        existing.setId(1L);
        existing.setNume("Reduceri de vara");
        existing.setProcentReducere(BigDecimal.TEN);
        existing.setDataStart(LocalDateTime.now());
        existing.setDataFinal(LocalDateTime.now().plusDays(1));
        existing.setActiva(true);

        Mockito.when(promotieService.getById(1L)).thenReturn(existing);
        Mockito.when(produsService.getAll()).thenReturn(List.of(new Produs()));
        Mockito.when(promotieService.update(Mockito.eq(1L), Mockito.any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data finala trebuie sa fie dupa data start"));

        mockMvc.perform(post("/web/promotii/1")
                        .param("nume", "Reduceri de vara")
                        .param("procentReducere", "15")
                        .param("dataStart", "2026-07-01T00:00")
                        .param("dataFinal", "2026-07-31T00:00")
                        .param("activa", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("promotii/form"))
                .andExpect(model().attribute("businessError", "Data finala trebuie sa fie dupa data start"));
    }

    @Test
    void update_valid_redirectsToList() throws Exception {
        Promotie updated = new Promotie();
        updated.setId(1L);
        updated.setNume("Reduceri de vara");
        Mockito.when(promotieService.update(Mockito.eq(1L), Mockito.any())).thenReturn(updated);

        mockMvc.perform(post("/web/promotii/1")
                        .param("nume", "Reduceri de vara")
                        .param("procentReducere", "15")
                        .param("dataStart", "2026-07-01T00:00")
                        .param("dataFinal", "2026-07-31T00:00")
                        .param("activa", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/promotii"));
    }

    @Test
    void delete_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/web/promotii/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/promotii"));

        Mockito.verify(promotieService).delete(1L);
    }

    @Test
    void delete_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Promotia este folosita"))
                .when(promotieService).delete(1L);

        mockMvc.perform(post("/web/promotii/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/promotii"))
                .andExpect(flash().attribute("businessError", "Promotia este folosita"));
    }

    @Test
    void addProdus_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este deja in promotie"))
                .when(promotieService).addProdus(1L, 2L);

        mockMvc.perform(post("/web/promotii/1/produse").param("produsId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/promotii/1/edit"))
                .andExpect(flash().attribute("businessError", "Produsul este deja in promotie"));
    }

    @Test
    void removeProdus_success_redirectsToEdit() throws Exception {
        mockMvc.perform(post("/web/promotii/1/produse/2/remove"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/promotii/1/edit"));

        Mockito.verify(promotieService).removeProdus(1L, 2L);
    }
}
