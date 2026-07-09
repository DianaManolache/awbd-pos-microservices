package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.client.SalesGateway;
import ro.facultate.pos.dto.AjusteazaStocRequest;
import ro.facultate.pos.dto.CreateProdusRequest;
import ro.facultate.pos.dto.UpdateProdusRequest;
import ro.facultate.pos.dto.UpdateStocRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.entity.Produs;
import ro.facultate.pos.event.StocEpuizatEvent;
import ro.facultate.pos.messaging.NotificationEventPublisher;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;
import ro.facultate.pos.repository.PromotieRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProdusService {

    private static final String CACHE = "produse";

    private static final Logger log = LoggerFactory.getLogger(ProdusService.class);

    private final ProdusRepository produsRepository;
    private final CategorieRepository categorieRepository;
    private final PromotieRepository promotieRepository;
    private final SalesGateway salesClient;
    private final NotificationEventPublisher notificationEventPublisher;

    public ProdusService(ProdusRepository produsRepository,
                          CategorieRepository categorieRepository,
                          PromotieRepository promotieRepository,
                          SalesGateway salesClient,
                          NotificationEventPublisher notificationEventPublisher) {
        this.produsRepository = produsRepository;
        this.categorieRepository = categorieRepository;
        this.promotieRepository = promotieRepository;
        this.salesClient = salesClient;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Produs create(CreateProdusRequest req) {
        Categorie categorie = categorieRepository.findById(req.getCategorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));

        Produs p = new Produs();
        p.setNume(req.getNume());
        p.setPret(req.getPret());
        p.setStoc(req.getStoc());
        p.setCategorie(categorie);

        Produs saved = produsRepository.save(p);
        log.info("Produs creat cu id {} in categoria {}", saved.getId(), categorie.getId());
        return saved;
    }

    @Cacheable(value = CACHE, key = "'all'")
    public List<Produs> getAll() {
        return produsRepository.findAll();
    }

    public Page<Produs> getPage(Pageable pageable) {
        return produsRepository.findAll(pageable);
    }

    @Cacheable(value = CACHE, key = "'categorie:' + #categorieId")
    public List<Produs> getByCategorie(Long categorieId) {
        return produsRepository.findByCategorieId(categorieId);
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Produs updateStoc(Long produsId, UpdateStocRequest req) {
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        produs.setStoc(req.getStoc());
        Produs saved = produsRepository.save(produs);
        log.debug("Stoc actualizat pentru produsul {}: {}", produsId, saved.getStoc());
        notificaDacaStocEpuizat(saved);
        return saved;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Produs ajusteazaStoc(Long produsId, AjusteazaStocRequest req) {
        Produs produs = produsRepository.findById(produsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));

        int stocNou = produs.getStoc() + req.getDelta();
        if (stocNou < 0) {
            log.info("Ajustare stoc respinsa pentru produsul {}: stoc insuficient", produsId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stoc insuficient");
        }

        produs.setStoc(stocNou);
        Produs saved = produsRepository.save(produs);
        log.debug("Stoc produs {} ajustat cu {} -> {}", produsId, req.getDelta(), saved.getStoc());
        notificaDacaStocEpuizat(saved);
        return saved;
    }

    /**
     * Calculeaza pretul dupa reducere folosind cea mai mare promotie
     * ACTIV activa chiar acum (interogare directa, niciodata cache-uita -
     * "activ acum" tine de ceasul curent, nu de datele statice ale
     * produsului, deci nu poate fi copleaseita in rezultatul @Cacheable
     * al getAll/getById fara sa devina stale).
     */
    public BigDecimal calculeazaPretEfectiv(Produs produs) {
        BigDecimal reducere = promotieRepository
                .findReducereMaximaActiva(produs.getId(), LocalDateTime.now())
                .orElse(BigDecimal.ZERO);

        if (reducere.compareTo(BigDecimal.ZERO) <= 0) {
            return produs.getPret();
        }

        BigDecimal factor = BigDecimal.ONE.subtract(reducere.divide(BigDecimal.valueOf(100)));
        return produs.getPret().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    public void aplicaPretEfectiv(Produs produs) {
        produs.setPretEfectiv(calculeazaPretEfectiv(produs));
    }

    public void aplicaPretEfectiv(List<Produs> produse) {
        produse.forEach(this::aplicaPretEfectiv);
    }

    private void notificaDacaStocEpuizat(Produs produs) {
        if (produs.getStoc() == 0) {
            notificationEventPublisher.publishStocEpuizat(
                    new StocEpuizatEvent(produs.getId(), produs.getNume(), produs.getStoc()));
        }
    }

    @Cacheable(value = CACHE, key = "#id")
    public Produs getById(Long id) {
        return produsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found"));
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public Produs update(Long id, UpdateProdusRequest req) {
        Produs produs = getById(id);

        Categorie categorie = categorieRepository.findById(req.getCategorieId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categorie not found"));

        produs.setNume(req.getNume());
        produs.setPret(req.getPret());
        produs.setStoc(req.getStoc());
        produs.setCategorie(categorie);

        Produs saved = produsRepository.save(produs);
        log.info("Produs actualizat cu id {}", saved.getId());
        return saved;
    }

    @CacheEvict(value = CACHE, allEntries = true)
    public void delete(Long id) {
        Produs produs = getById(id);

        if (salesClient.produsExistaPeBon(id)) {
            log.info("Stergere produs {} respinsa: este pe cel putin un bon", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este pe cel putin un bon");
        }
        if (promotieRepository.existsByProduseId(id)) {
            log.info("Stergere produs {} respinsa: este intr-o promotie activa", id);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produsul este intr-o promotie activa");
        }

        produsRepository.delete(produs);
        log.info("Produs sters cu id {}", id);
    }
}
