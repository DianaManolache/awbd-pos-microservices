package ro.facultate.pos.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.facultate.pos.dto.*;
import ro.facultate.pos.entity.Bon;
import ro.facultate.pos.entity.enums.TipPlata;
import ro.facultate.pos.service.BonService;
import ro.facultate.pos.service.ClientService;
import ro.facultate.pos.service.ProdusService;
import ro.facultate.pos.service.VanzatorService;

@Controller
@RequestMapping("/web/bonuri")
public class BonViewController {

    private final BonService bonService;
    private final ClientService clientService;
    private final VanzatorService vanzatorService;
    private final ProdusService produsService;

    public BonViewController(BonService bonService, ClientService clientService,
                              VanzatorService vanzatorService, ProdusService produsService) {
        this.bonService = bonService;
        this.clientService = clientService;
        this.vanzatorService = vanzatorService;
        this.produsService = produsService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("bonuri", bonService.getAll());
        return "bonuri/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("bon", new CreateBonRequest());
        model.addAttribute("clienti", clientService.getAll());
        model.addAttribute("vanzatori", vanzatorService.getAll());
        return "bonuri/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("bon") CreateBonRequest bon,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("clienti", clientService.getAll());
            model.addAttribute("vanzatori", vanzatorService.getAll());
            return "bonuri/new";
        }
        Bon saved;
        try {
            saved = bonService.create(bon);
        } catch (ResponseStatusException e) {
            model.addAttribute("businessError", e.getReason());
            model.addAttribute("clienti", clientService.getAll());
            model.addAttribute("vanzatori", vanzatorService.getAll());
            return "bonuri/new";
        }
        return "redirect:/web/bonuri/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        BonDetailsResponse details = bonService.getDetails(id);

        model.addAttribute("bon", details);
        model.addAttribute("client", clientService.getById(details.getClientId()));
        model.addAttribute("vanzator", vanzatorService.getById(details.getVanzatorId()));
        model.addAttribute("plati", bonService.getPlati(id));
        model.addAttribute("produseDisponibile", produsService.getAll());
        model.addAttribute("addProdusForm", new AddBonProdusRequest());
        model.addAttribute("payForm", new PayBonRequest());
        return "bonuri/detail";
    }

    @PostMapping("/{id}/produse")
    public String addProdus(@PathVariable Long id, @ModelAttribute AddBonProdusRequest addProdusForm,
                             RedirectAttributes redirectAttributes) {
        try {
            bonService.addProdus(id, addProdusForm);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/bonuri/" + id;
    }

    @PostMapping("/{id}/produse/{lineId}/update")
    public String updateLine(@PathVariable Long id, @PathVariable Long lineId,
                              @RequestParam Integer cantitate, RedirectAttributes redirectAttributes) {
        UpdateBonProdusRequest req = new UpdateBonProdusRequest();
        req.setCantitate(cantitate);
        try {
            bonService.updateBonProdus(id, lineId, req);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/bonuri/" + id;
    }

    @PostMapping("/{id}/produse/{lineId}/delete")
    public String deleteLine(@PathVariable Long id, @PathVariable Long lineId,
                              RedirectAttributes redirectAttributes) {
        try {
            bonService.deleteBonProdus(id, lineId);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/bonuri/" + id;
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam TipPlata tipPlata,
                       RedirectAttributes redirectAttributes) {
        try {
            bonService.payBon(id, tipPlata);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/bonuri/" + id;
    }

    @PostMapping("/{id}/plati/{plataId}/delete")
    public String deletePlata(@PathVariable Long id, @PathVariable Long plataId,
                               RedirectAttributes redirectAttributes) {
        try {
            bonService.deletePlata(id, plataId);
        } catch (ResponseStatusException e) {
            redirectAttributes.addFlashAttribute("businessError", e.getReason());
        }
        return "redirect:/web/bonuri/" + id;
    }
}
