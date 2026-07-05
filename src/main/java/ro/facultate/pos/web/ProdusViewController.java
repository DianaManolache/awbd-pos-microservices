package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.CreateProdusRequest;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.service.CategorieService;
import ro.facultate.pos.service.ProdusService;

@Controller
@RequestMapping("/web/produse")
public class ProdusViewController {

    private final ProdusService produsService;
    private final CategorieService categorieService;

    public ProdusViewController(ProdusService produsService, CategorieService categorieService) {
        this.produsService = produsService;
        this.categorieService = categorieService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("produse", produsService.getAll());
        return "produse/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("produs", new CreateProdusRequest());
        model.addAttribute("categorii", categorieService.getAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("formAction", "/web/produse");
        return "produse/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("produs") CreateProdusRequest produs,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorii", categorieService.getAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/produse");
            return "produse/form";
        }
        try {
            produsService.create(produs);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("categorii", categorieService.getAll());
            model.addAttribute("isEdit", false);
            model.addAttribute("formAction", "/web/produse");
            return "produse/form";
        }
        return "redirect:/web/produse";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Produs produs = produsService.getById(id);

        UpdateProdusRequest form = new UpdateProdusRequest();
        form.setNume(produs.getNume());
        form.setPret(produs.getPret());
        form.setStoc(produs.getStoc());
        form.setCategorieId(produs.getCategorie().getId());

        model.addAttribute("produs", form);
        model.addAttribute("categorii", categorieService.getAll());
        model.addAttribute("isEdit", true);
        model.addAttribute("formAction", "/web/produse/" + id);
        return "produse/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("produs") UpdateProdusRequest produs,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorii", categorieService.getAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/produse/" + id);
            return "produse/form";
        }
        try {
            produsService.update(id, produs);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("categorii", categorieService.getAll());
            model.addAttribute("isEdit", true);
            model.addAttribute("formAction", "/web/produse/" + id);
            return "produse/form";
        }
        return "redirect:/web/produse";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            produsService.delete(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/produse";
    }
}
