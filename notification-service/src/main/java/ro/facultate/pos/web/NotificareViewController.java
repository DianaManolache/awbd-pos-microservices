package ro.facultate.pos.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ro.facultate.pos.entity.Notificare;
import ro.facultate.pos.service.NotificareService;

@Controller
@RequestMapping("/web/notificari")
public class NotificareViewController {

    private final NotificareService notificareService;

    public NotificareViewController(NotificareService notificareService) {
        this.notificareService = notificareService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Page<Notificare> notificariPage = notificareService.getPage(
                PageRequest.of(page, size, Sort.by("primitaLa").descending()));

        model.addAttribute("notificariPage", notificariPage);
        model.addAttribute("size", size);
        return "notificari/list";
    }
}
