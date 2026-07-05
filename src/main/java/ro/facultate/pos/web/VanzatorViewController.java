package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.CreateVanzatorRequest;
import ro.facultate.pos.dto.UpdateVanzatorRequest;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.service.VanzatorService;

@Controller
@RequestMapping("/web/vanzatori")
public class VanzatorViewController {

    private final VanzatorService vanzatorService;

    public VanzatorViewController(VanzatorService vanzatorService) {
        this.vanzatorService = vanzatorService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vanzatori", vanzatorService.getAll());
        return "vanzatori/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("vanzator", new CreateVanzatorRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/web/vanzatori");
        return "vanzatori/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("vanzator") CreateVanzatorRequest vanzator,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/vanzatori");
            return "vanzatori/form";
        }
        try {
            vanzatorService.create(vanzator);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/vanzatori");
            return "vanzatori/form";
        }
        return "redirect:/web/vanzatori";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Vanzator vanzator = vanzatorService.getById(id);

        UpdateVanzatorRequest form = new UpdateVanzatorRequest();
        form.setNume(vanzator.getNume());

        model.addAttribute("vanzator", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/web/vanzatori/" + id);
        return "vanzatori/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("vanzator") UpdateVanzatorRequest vanzator,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/vanzatori/" + id);
            return "vanzatori/form";
        }
        try {
            vanzatorService.update(id, vanzator);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/vanzatori/" + id);
            return "vanzatori/form";
        }
        return "redirect:/web/vanzatori";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vanzatorService.delete(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/vanzatori";
    }
}
