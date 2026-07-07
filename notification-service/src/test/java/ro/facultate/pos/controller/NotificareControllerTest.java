package ro.facultate.pos.controller;

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
@WebMvcTest(NotificareController.class)
class NotificareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificareService notificareService;

    @Test
    void getPage_returnsNotificari() throws Exception {
        Notificare n = new Notificare("BON_PLATIT", "Bon 1 platit", Map.of("bonId", 1), Instant.now());
        Mockito.when(notificareService.getPage(Mockito.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(n)));

        mockMvc.perform(get("/api/notificari"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tip").value("BON_PLATIT"));
    }
}
