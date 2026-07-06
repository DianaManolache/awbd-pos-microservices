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
        populateFormModel(model, false, null);
        return "vanzatori/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("vanzator") CreateVanzatorRequest vanzator,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, false, null);
            return "vanzatori/form";
        }
        try {
            vanzatorService.create(vanzator);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, false, null);
            return "vanzatori/form";
        }
        return "redirect:/web/vanzatori";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Vanzator vanzator;
        try {
            vanzator = vanzatorService.getById(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
            return "redirect:/web/vanzatori";
        }

        UpdateVanzatorRequest form = new UpdateVanzatorRequest();
        form.setNume(vanzator.getNume());

        model.addAttribute("vanzator", form);
        populateFormModel(model, true, id);
        return "vanzatori/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("vanzator") UpdateVanzatorRequest vanzator,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, true, id);
            return "vanzatori/form";
        }
        try {
            vanzatorService.update(id, vanzator);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, true, id);
            return "vanzatori/form";
        }
        return "redirect:/web/vanzatori";
    }

    private void populateFormModel(Model model, boolean isEdit, Long id) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/web/vanzatori/" + id : "/web/vanzatori");
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
