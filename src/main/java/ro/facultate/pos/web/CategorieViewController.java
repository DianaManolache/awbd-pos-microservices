package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.CreateCategorieRequest;
import ro.facultate.pos.dto.UpdateCategorieRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.service.CategorieService;

@Controller
@RequestMapping("/web/categorii")
public class CategorieViewController {

    private final CategorieService categorieService;

    public CategorieViewController(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categorii", categorieService.getAll());
        return "categorii/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("categorie", new CreateCategorieRequest());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/web/categorii");
        return "categorii/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("categorie") CreateCategorieRequest categorie,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/categorii");
            return "categorii/form";
        }
        try {
            categorieService.create(categorie);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/categorii");
            return "categorii/form";
        }
        return "redirect:/web/categorii";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Categorie categorie = categorieService.getById(id);

        UpdateCategorieRequest form = new UpdateCategorieRequest();
        form.setNume(categorie.getNume());

        model.addAttribute("categorie", form);
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/web/categorii/" + id);
        return "categorii/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("categorie") UpdateCategorieRequest categorie,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/categorii/" + id);
            return "categorii/form";
        }
        try {
            categorieService.update(id, categorie);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/categorii/" + id);
            return "categorii/form";
        }
        return "redirect:/web/categorii";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categorieService.delete(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/categorii";
    }
}
