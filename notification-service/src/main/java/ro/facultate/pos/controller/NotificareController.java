package ro.facultate.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ro.facultate.pos.entity.Notificare;
import ro.facultate.pos.service.NotificareService;

@Tag(name = "Notificari", description = "Notificari generate din evenimente de business (mesagerie)")
@RestController
@RequestMapping("/api/notificari")
public class NotificareController {

    private final NotificareService notificareService;

    public NotificareController(NotificareService notificareService) {
        this.notificareService = notificareService;
    }

    @Operation(summary = "Listeaza notificari", description = "Returneaza notificarile primite, cele mai recente primele")
    @GetMapping
    public Page<Notificare> getPage(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return notificareService.getPage(PageRequest.of(page, size, Sort.by("primitaLa").descending()));
    }
}
