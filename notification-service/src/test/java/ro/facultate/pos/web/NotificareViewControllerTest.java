package ro.facultate.pos.web;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import ro.facultate.pos.entity.Notificare;
import ro.facultate.pos.service.NotificareService;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(NotificareViewController.class)
class NotificareViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificareService notificareService;

    @Test
    void list_rendersListView() throws Exception {
        Notificare n = new Notificare("STOC_EPUIZAT", "Produsul X a ramas fara stoc", Map.of(), Instant.now());
        Mockito.when(notificareService.getPage(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(n)));

        mockMvc.perform(get("/web/notificari"))
                .andExpect(status().isOk())
                .andExpect(view().name("notificari/list"));
    }
}
