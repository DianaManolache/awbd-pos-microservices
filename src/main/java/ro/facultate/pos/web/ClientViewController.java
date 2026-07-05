package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.CreateClientRequest;
import ro.facultate.pos.dto.UpdateClientRequest;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.service.ClientService;

@Controller
@RequestMapping("/web/clienti")
public class ClientViewController {

    private final ClientService clientService;

    public ClientViewController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("clienti", clientService.getAll());
        return "clienti/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("client", new CreateClientRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/web/clienti");
        return "clienti/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("client") CreateClientRequest client,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/clienti");
            return "clienti/form";
        }
        try {
            clientService.create(client);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/clienti");
            return "clienti/form";
        }
        return "redirect:/web/clienti";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Client client = clientService.getById(id);

        UpdateClientRequest form = new UpdateClientRequest();
        form.setNume(client.getNume());
        form.setEmail(client.getEmail());
        form.setTelefon(client.getTelefon());

        model.addAttribute("client", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/web/clienti/" + id);
        return "clienti/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("client") UpdateClientRequest client,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/clienti/" + id);
            return "clienti/form";
        }
        try {
            clientService.update(id, client);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/clienti/" + id);
            return "clienti/form";
        }
        return "redirect:/web/clienti";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.delete(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/clienti";
    }
}
