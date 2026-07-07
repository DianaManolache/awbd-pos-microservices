package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @RequestParam(defaultValue = "nume") String sort,
                        @RequestParam(defaultValue = "asc") String dir,
                        Model model) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<Client> clientiPage = clientService.getPage(pageable);

        model.addAttribute("clientiPage", clientiPage);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("size", size);
        return "clienti/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("client", new CreateClientRequest());
        populateFormModel(model, false, null);
        return "clienti/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("client") CreateClientRequest client,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, false, null);
            return "clienti/form";
        }
        try {
            clientService.create(client);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, false, null);
            return "clienti/form";
        }
        return "redirect:/web/clienti";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Client client;
        try {
            client = clientService.getById(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
            return "redirect:/web/clienti";
        }

        UpdateClientRequest form = new UpdateClientRequest();
        form.setNume(client.getNume());
        form.setEmail(client.getEmail());
        form.setTelefon(client.getTelefon());

        model.addAttribute("client", form);
        populateFormModel(model, true, id);
        return "clienti/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("client") UpdateClientRequest client,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, true, id);
            return "clienti/form";
        }
        try {
            clientService.update(id, client);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, true, id);
            return "clienti/form";
        }
        return "redirect:/web/clienti";
    }

    private void populateFormModel(Model model, boolean isEdit, Long id) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/web/clienti/" + id : "/web/clienti");
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
