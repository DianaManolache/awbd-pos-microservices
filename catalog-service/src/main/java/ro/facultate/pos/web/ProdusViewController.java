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
    public String list(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @RequestParam(defaultValue = "nume") String sort,
                        @RequestParam(defaultValue = "asc") String dir,
                        Model model) {
        Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<Produs> produsePage = produsService.getPage(pageable);

        model.addAttribute("produsePage", produsePage);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("size", size);
        return "produse/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("produs", new CreateProdusRequest());
        populateFormModel(model, false, null);
        return "produse/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("produs") CreateProdusRequest produs,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, false, null);
            return "produse/form";
        }
        try {
            produsService.create(produs);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, false, null);
            return "produse/form";
        }
        return "redirect:/web/produse";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Produs produs;
        try {
            produs = produsService.getById(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
            return "redirect:/web/produse";
        }

        UpdateProdusRequest form = new UpdateProdusRequest();
        form.setNume(produs.getNume());
        form.setPret(produs.getPret());
        form.setStoc(produs.getStoc());
        form.setCategorieId(produs.getCategorie().getId());

        model.addAttribute("produs", form);
        populateFormModel(model, true, id);
        return "produse/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("produs") UpdateProdusRequest produs,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, true, id);
            return "produse/form";
        }
        try {
            produsService.update(id, produs);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, true, id);
            return "produse/form";
        }
        return "redirect:/web/produse";
    }

    private void populateFormModel(Model model, boolean isEdit, Long id) {
        model.addAttribute("categorii", categorieService.getAll());
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/web/produse/" + id : "/web/produse");
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
