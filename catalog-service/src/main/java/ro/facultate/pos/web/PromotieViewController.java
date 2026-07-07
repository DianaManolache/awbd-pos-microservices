package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.CreatePromotieRequest;
import ro.facultate.pos.dto.UpdatePromotieRequest;
import ro.facultate.pos.entity.Promotie;
import ro.facultate.pos.service.ProdusService;
import ro.facultate.pos.service.PromotieService;

@Controller
@RequestMapping("/web/promotii")
public class PromotieViewController {

    private final PromotieService promotieService;
    private final ProdusService produsService;

    public PromotieViewController(PromotieService promotieService, ProdusService produsService) {
        this.promotieService = promotieService;
        this.produsService = produsService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("promotii", promotieService.getAll());
        return "promotii/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("promotie", new CreatePromotieRequest());
        populateFormModel(model, false, null);
        return "promotii/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("promotie") CreatePromotieRequest promotie,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, false, null);
            return "promotii/form";
        }
        try {
            promotieService.create(promotie);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, false, null);
            return "promotii/form";
        }
        return "redirect:/web/promotii";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Promotie promotie;
        try {
            promotie = promotieService.getById(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
            return "redirect:/web/promotii";
        }

        UpdatePromotieRequest form = new UpdatePromotieRequest();
        form.setNume(promotie.getNume());
        form.setProcentReducere(promotie.getProcentReducere());
        form.setDataStart(promotie.getDataStart());
        form.setDataFinal(promotie.getDataFinal());
        form.setActiva(promotie.getActiva());

        model.addAttribute("promotie", form);
        populateFormModel(model, true, id);
        return "promotii/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                          @Valid @ModelAttribute("promotie") UpdatePromotieRequest promotie,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateFormModel(model, true, id);
            return "promotii/form";
        }
        try {
            promotieService.update(id, promotie);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            populateFormModel(model, true, id);
            return "promotii/form";
        }
        return "redirect:/web/promotii";
    }

    private void populateFormModel(Model model, boolean isEdit, Long id) {
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("formAction", isEdit ? "/web/promotii/" + id : "/web/promotii");
        if (isEdit) {
            Promotie existing = promotieService.getById(id);
            model.addAttribute("promotieId", id);
            model.addAttribute("produseAsociate", existing.getProduse());
            model.addAttribute("produseDisponibile", produsService.getAll());
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promotieService.delete(id);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/promotii";
    }

    @PostMapping("/{id}/produse")
    public String addProdus(@PathVariable Long id, @RequestParam Long produsId,
                             RedirectAttributes redirectAttributes) {
        try {
            promotieService.addProdus(id, produsId);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/promotii/" + id + "/edit";
    }

    @PostMapping("/{id}/produse/{produsId}/remove")
    public String removeProdus(@PathVariable Long id, @PathVariable Long produsId,
                                RedirectAttributes redirectAttributes) {
        try {
            promotieService.removeProdus(id, produsId);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/promotii/" + id + "/edit";
    }
}
