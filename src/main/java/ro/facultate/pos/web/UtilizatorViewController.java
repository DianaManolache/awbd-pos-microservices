package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.CreateUtilizatorRequest;
import ro.facultate.pos.dto.UpdateUtilizatorRequest;
import ro.facultate.pos.entity.Utilizator;
import ro.facultate.pos.entity.enums.RolUtilizator;
import ro.facultate.pos.service.UtilizatorService;
import ro.facultate.pos.service.VanzatorService;

@Controller
@RequestMapping("/web/utilizatori")
public class UtilizatorViewController {

    private final UtilizatorService utilizatorService;
    private final VanzatorService vanzatorService;

    public UtilizatorViewController(UtilizatorService utilizatorService, VanzatorService vanzatorService) {
        this.utilizatorService = utilizatorService;
        this.vanzatorService = vanzatorService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("utilizatori", utilizatorService.getAll());
        return "utilizatori/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("utilizator", new CreateUtilizatorRequest());
        model.addAttribute("vanzatori", vanzatorService.getAll());
        model.addAttribute("roluri", RolUtilizator.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/web/utilizatori");
        return "utilizatori/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("utilizator") CreateUtilizatorRequest utilizator,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("vanzatori", vanzatorService.getAll());
            model.addAttribute("roluri", RolUtilizator.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/utilizatori");
            return "utilizatori/form";
        }
        try {
            utilizatorService.create(utilizator);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("vanzatori", vanzatorService.getAll());
            model.addAttribute("roluri", RolUtilizator.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/utilizatori");
            return "utilizatori/form";
        }
        return "redirect:/web/utilizatori";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Utilizator utilizator = utilizatorService.getById(id);

        UpdateUtilizatorRequest form = new UpdateUtilizatorRequest();
        form.setUsername(utilizator.getUsername());
        form.setRol(utilizator.getRol());
        form.setActiv(utilizator.getActiv());

        model.addAttribute("utilizator", form);
        model.addAttribute("roluri", RolUtilizator.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/web/utilizatori/" + id);
        return "utilizatori/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("utilizator") UpdateUtilizatorRequest utilizator,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roluri", RolUtilizator.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/utilizatori/" + id);
            return "utilizatori/form";
        }
        try {
            utilizatorService.update(id, utilizator);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("roluri", RolUtilizator.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/utilizatori/" + id);
            return "utilizatori/form";
        }
        return "redirect:/web/utilizatori";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            utilizatorService.delete(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/utilizatori";
    }
}
