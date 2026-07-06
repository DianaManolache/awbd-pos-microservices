package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.BonDetailsResponse;
import ro.facultate.pos.entity.Bon;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.entity.enums.BonStatus;
import ro.facultate.pos.service.BonService;
import ro.facultate.pos.service.ClientService;
import ro.facultate.pos.service.ProdusService;
import ro.facultate.pos.service.VanzatorService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BonViewController.class)
class BonViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BonService bonService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private VanzatorService vanzatorService;

    @MockBean
    private ProdusService produsService;

    @Test
    void list_rendersListView() throws Exception {
        Bon b = new Bon();
        b.setId(1L);
        b.setStatus(BonStatus.OPEN);
        Mockito.when(bonService.getPage(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(b)));

        mockMvc.perform(get("/web/bonuri"))
                .andExpect(status().isOk())
                .andExpect(view().name("bonuri/list"));
    }

    @Test
    void list_defaultSort_usesDataDescending() throws Exception {
        Mockito.when(bonService.getPage(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/web/bonuri"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(bonService).getPage(captor.capture());
        Pageable used = captor.getValue();
        assertEquals(0, used.getPageNumber());
        assertEquals(5, used.getPageSize());
        assertEquals(Sort.Direction.DESC, used.getSort().getOrderFor("data").getDirection());
    }

    @Test
    void list_customSortAndSize_buildsPageableCorrectly() throws Exception {
        Mockito.when(bonService.getPage(Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/web/bonuri").param("page", "1").param("size", "10").param("sort", "status").param("dir", "asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(bonService).getPage(captor.capture());
        Pageable used = captor.getValue();
        assertEquals(1, used.getPageNumber());
        assertEquals(10, used.getPageSize());
        assertEquals(Sort.Direction.ASC, used.getSort().getOrderFor("status").getDirection());
    }

    @Test
    void newForm_rendersFormView() throws Exception {
        mockMvc.perform(get("/web/bonuri/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("bonuri/new"));
    }

    @Test
    void create_valid_redirectsToDetail() throws Exception {
        Bon saved = new Bon();
        saved.setId(10L);
        Mockito.when(bonService.create(Mockito.any())).thenReturn(saved);

        mockMvc.perform(post("/web/bonuri").param("clientId", "1").param("vanzatorId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"));
    }

    @Test
    void create_invalid_rerendersFormAndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/web/bonuri").param("vanzatorId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("bonuri/new"));

        Mockito.verify(bonService, Mockito.never()).create(Mockito.any());
    }

    @Test
    void detail_rendersDetailView() throws Exception {
        BonDetailsResponse details = new BonDetailsResponse();
        details.setId(10L);
        details.setStatus(BonStatus.OPEN);
        details.setClientId(1L);
        details.setVanzatorId(1L);
        details.setTotal(BigDecimal.ZERO);
        details.setProduse(List.of());

        Mockito.when(bonService.getDetails(10L)).thenReturn(details);
        Mockito.when(clientService.getById(1L)).thenReturn(new Client());
        Mockito.when(vanzatorService.getById(1L)).thenReturn(new Vanzator());
        Mockito.when(bonService.getPlati(10L)).thenReturn(List.of());
        Mockito.when(produsService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/web/bonuri/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("bonuri/detail"));
    }

    @Test
    void detail_idNotFound_redirectsToListWithFlashMessage() throws Exception {
        Mockito.when(bonService.getDetails(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Bonul nu exista"));

        mockMvc.perform(get("/web/bonuri/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri"))
                .andExpect(flash().attribute("businessError", "Bonul nu exista"));
    }

    @Test
    void addProdus_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stoc insuficient"))
                .when(bonService).addProdus(Mockito.eq(10L), Mockito.any());

        mockMvc.perform(post("/web/bonuri/10/produse").param("produsId", "1").param("cantitate", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attribute("businessError", "Stoc insuficient"));
    }

    @Test
    void updateLine_success_redirectsAndCallsService() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/produse/2/update").param("cantitate", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"));

        Mockito.verify(bonService).updateBonProdus(Mockito.eq(10L), Mockito.eq(2L), Mockito.any());
    }

    @Test
    void updateLine_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stoc insuficient"))
                .when(bonService).updateBonProdus(Mockito.eq(10L), Mockito.eq(2L), Mockito.any());

        mockMvc.perform(post("/web/bonuri/10/produse/2/update").param("cantitate", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attribute("businessError", "Stoc insuficient"));
    }

    @Test
    void updateLine_invalidCantitate_redirectsWithFlashMessageAndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/produse/2/update").param("cantitate", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attributeExists("businessError"));

        Mockito.verify(bonService, Mockito.never()).updateBonProdus(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void updateLine_missingCantitate_redirectsWithFlashMessageAndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/produse/2/update"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attributeExists("businessError"));

        Mockito.verify(bonService, Mockito.never()).updateBonProdus(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void deleteLine_success_redirectsAndCallsService() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/produse/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"));

        Mockito.verify(bonService).deleteBonProdus(10L, 2L);
    }

    @Test
    void deleteLine_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul nu este OPEN"))
                .when(bonService).deleteBonProdus(10L, 2L);

        mockMvc.perform(post("/web/bonuri/10/produse/2/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attribute("businessError", "Bonul nu este OPEN"));
    }

    @Test
    void pay_success_redirectsToDetail() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/pay").param("tipPlata", "CASH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"));

        Mockito.verify(bonService).payBon(10L, ro.facultate.pos.entity.enums.TipPlata.CASH);
    }

    @Test
    void pay_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.when(bonService.payBon(10L, ro.facultate.pos.entity.enums.TipPlata.CASH))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul nu este OPEN"));

        mockMvc.perform(post("/web/bonuri/10/pay").param("tipPlata", "CASH"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attribute("businessError", "Bonul nu este OPEN"));
    }

    @Test
    void pay_missingTipPlata_redirectsWithFlashMessageAndDoesNotCallService() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/pay"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attributeExists("businessError"));

        Mockito.verify(bonService, Mockito.never()).payBon(Mockito.any(), Mockito.any());
    }

    @Test
    void deletePlata_success_redirectsAndCallsService() throws Exception {
        mockMvc.perform(post("/web/bonuri/10/plati/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"));

        Mockito.verify(bonService).deletePlata(10L, 5L);
    }

    @Test
    void deletePlata_businessError_redirectsWithFlashMessage() throws Exception {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nu se poate sterge o plata cu status SUCCESS"))
                .when(bonService).deletePlata(10L, 5L);

        mockMvc.perform(post("/web/bonuri/10/plati/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/bonuri/10"))
                .andExpect(flash().attribute("businessError", "Nu se poate sterge o plata cu status SUCCESS"));
    }
}
