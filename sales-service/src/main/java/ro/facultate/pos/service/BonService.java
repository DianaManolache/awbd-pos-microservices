package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.client.CatalogGateway;
import ro.facultate.pos.dto.CreateBonRequest;
import ro.facultate.pos.dto.ProdusResponse;
import ro.facultate.pos.dto.UpdateBonRequest;
import ro.facultate.pos.entity.Bon;
import ro.facultate.pos.entity.Client;
import ro.facultate.pos.entity.Vanzator;
import ro.facultate.pos.entity.enums.BonStatus;
import ro.facultate.pos.entity.enums.TipPlata;
import ro.facultate.pos.exception.BusinessException;
import ro.facultate.pos.repository.BonRepository;
import ro.facultate.pos.repository.ClientRepository;
import ro.facultate.pos.repository.VanzatorRepository;
import ro.facultate.pos.dto.AddBonProdusRequest;
import ro.facultate.pos.entity.BonProdus;
import ro.facultate.pos.repository.BonProdusRepository;
import ro.facultate.pos.dto.BonDetailsResponse;
import ro.facultate.pos.dto.BonProdusLineResponse;
import ro.facultate.pos.entity.Plata;
import ro.facultate.pos.entity.enums.StatusPlata;
import ro.facultate.pos.repository.PlataRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Service
public class BonService {

    private static final Logger log = LoggerFactory.getLogger(BonService.class);

    private final BonRepository bonRepository;
    private final ClientRepository clientRepository;
    private final VanzatorRepository vanzatorRepository;
    private final BonProdusRepository bonProdusRepository;
    private final PlataRepository plataRepository;
    private final CatalogGateway catalogClient;

    public BonService(BonRepository bonRepository,
                      ClientRepository clientRepository,
                      VanzatorRepository vanzatorRepository,
                      BonProdusRepository bonProdusRepository,
                      PlataRepository plataRepository,
                      CatalogGateway catalogClient) {
        this.bonRepository = bonRepository;
        this.clientRepository = clientRepository;
        this.vanzatorRepository = vanzatorRepository;
        this.bonProdusRepository = bonProdusRepository;
        this.plataRepository = plataRepository;
        this.catalogClient = catalogClient;
    }

    public Bon create(CreateBonRequest req) {
        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        Vanzator vanzator = vanzatorRepository.findById(req.getVanzatorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vanzator not found"));

        Bon bon = new Bon();
        bon.setData(LocalDateTime.now());
        bon.setStatus(BonStatus.OPEN);
        bon.setClient(client);
        bon.setVanzator(vanzator);

        Bon saved = bonRepository.save(bon);
        log.info("Bon creat cu id {} pentru clientul {} si vanzatorul {}", saved.getId(), client.getId(), vanzator.getId());
        return saved;
    }

    public List<Bon> getAll() {
        return bonRepository.findAll();
    }

    public Page<Bon> getPage(Pageable pageable) {
        return bonRepository.findAll(pageable);
    }

    public Bon update(Long id, UpdateBonRequest req) {
        Bon bon = bonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        if (bon.getStatus() != BonStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul nu este OPEN");
        }
        if (!bonProdusRepository.findByBonId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul are produse adaugate");
        }

        Client client = clientRepository.findById(req.getClientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));
        Vanzator vanzator = vanzatorRepository.findById(req.getVanzatorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vanzator not found"));

        bon.setClient(client);
        bon.setVanzator(vanzator);

        return bonRepository.save(bon);
    }

    public void delete(Long id) {
        Bon bon = bonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        if (bon.getStatus() != BonStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul nu este OPEN");
        }
        if (!bonProdusRepository.findByBonId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul are produse adaugate");
        }
        if (!plataRepository.findByBonId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul are plati asociate");
        }

        bonRepository.delete(bon);
    }

    public boolean existaProdusPeVreunBon(Long produsId) {
        return bonProdusRepository.existsByProdusId(produsId);
    }

    public BonProdus addProdus(Long bonId, AddBonProdusRequest req) {

        Bon bon = bonRepository.findById(bonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        if (bon.getStatus() != BonStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bon is not OPEN");
        }

        ProdusResponse produs = catalogClient.getProdus(req.getProdusId());
        if (produs == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produs not found");
        }

        try {
            catalogClient.ajusteazaStoc(req.getProdusId(), Map.of("delta", -req.getCantitate()));
        } catch (feign.FeignException.BadRequest e) {
            log.info("Adaugare produs {} pe bonul {} respinsa: stoc insuficient", req.getProdusId(), bonId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stoc insuficient");
        }
        log.debug("Stoc produs {} redus cu {} dupa adaugare pe bonul {}", req.getProdusId(), req.getCantitate(), bonId);

        BonProdus bp = new BonProdus();
        bp.setBon(bon);
        bp.setProdusId(produs.getId());
        bp.setProdusNume(produs.getNume());
        bp.setCantitate(req.getCantitate());
        bp.setPretUnitar(produs.getPret());

        BonProdus saved;
        try {
            saved = bonProdusRepository.save(bp);
        } catch (RuntimeException e) {
            catalogClient.ajusteazaStoc(req.getProdusId(), Map.of("delta", req.getCantitate()));
            throw e;
        }
        log.info("Produs {} adaugat pe bonul {} (cantitate {})", produs.getId(), bonId, req.getCantitate());
        return saved;
    }

    public BonProdus updateBonProdus(Long bonId, Long bonProdusId, ro.facultate.pos.dto.UpdateBonProdusRequest req) {
        Bon bon = bonRepository.findById(bonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        if (bon.getStatus() != BonStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul nu este OPEN");
        }

        BonProdus line = bonProdusRepository.findById(bonProdusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linie inexistenta"));

        if (!line.getBon().getId().equals(bonId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Linia nu apartine acestui bon");
        }

        int delta = req.getCantitate() - line.getCantitate();

        try {
            catalogClient.ajusteazaStoc(line.getProdusId(), Map.of("delta", -delta));
        } catch (feign.FeignException.BadRequest e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stoc insuficient");
        }

        line.setCantitate(req.getCantitate());
        return bonProdusRepository.save(line);
    }

    public void deleteBonProdus(Long bonId, Long bonProdusId) {
        Bon bon = bonRepository.findById(bonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        if (bon.getStatus() != BonStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bonul nu este OPEN");
        }

        BonProdus line = bonProdusRepository.findById(bonProdusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Linie inexistenta"));

        if (!line.getBon().getId().equals(bonId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Linia nu apartine acestui bon");
        }

        catalogClient.ajusteazaStoc(line.getProdusId(), Map.of("delta", line.getCantitate()));

        bonProdusRepository.delete(line);
    }

    public BonDetailsResponse getDetails(Long bonId) {
        Bon bon = bonRepository.findById(bonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        List<BonProdus> lines = bonProdusRepository.findByBonId(bonId);

        BigDecimal total = BigDecimal.ZERO;
        List<BonProdusLineResponse> produse = new ArrayList<>();

        for (BonProdus line : lines) {
            BonProdusLineResponse r = new BonProdusLineResponse();
            r.setId(line.getId());
            r.setProdusId(line.getProdusId());
            r.setProdusNume(line.getProdusNume());
            r.setCantitate(line.getCantitate());
            r.setPretUnitar(line.getPretUnitar());

            BigDecimal totalLinie = line.getPretUnitar()
                    .multiply(BigDecimal.valueOf(line.getCantitate()));
            r.setTotalLinie(totalLinie);

            total = total.add(totalLinie);
            produse.add(r);
        }

        BonDetailsResponse resp = new BonDetailsResponse();
        resp.setId(bon.getId());
        resp.setData(bon.getData());
        resp.setStatus(bon.getStatus());
        resp.setClientId(bon.getClient().getId());
        resp.setVanzatorId(bon.getVanzator().getId());
        resp.setProduse(produse);
        resp.setTotal(total);

        return resp;
    }

    public Plata payBon(Long bonId, TipPlata tipPlata) {
        try {
            Bon bon = bonRepository.findById(bonId)
                    .orElseThrow(() -> new BusinessException("Bon inexistent"));

            if (bon.getStatus() != BonStatus.OPEN) {
                throw new BusinessException("Bonul nu este OPEN");
            }

            List<BonProdus> lines = bonProdusRepository.findByBonId(bonId);
            BigDecimal total = BigDecimal.ZERO;

            for (BonProdus line : lines) {
                total = total.add(
                        line.getPretUnitar()
                                .multiply(BigDecimal.valueOf(line.getCantitate()))
                );
            }

            Plata plata = new Plata();
            plata.setBon(bon);
            plata.setTip(tipPlata);
            plata.setSuma(total);
            plata.setData(LocalDateTime.now());
            plata.setStatus(StatusPlata.SUCCESS);

            plataRepository.save(plata);

            bon.setStatus(BonStatus.PAID);
            bonRepository.save(bon);
            log.info("Bon {} platit ({}), suma {}", bonId, tipPlata, total);

            return plata;

        } catch (BusinessException e) {
            log.info("Plata bonului {} respinsa: {}", bonId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public List<Plata> getPlati(Long bonId) {
        bonRepository.findById(bonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bon not found"));

        return plataRepository.findByBonId(bonId);
    }

    public Plata getPlata(Long bonId, Long plataId) {
        Plata plata = plataRepository.findById(plataId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plata not found"));

        if (!plata.getBon().getId().equals(bonId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plata nu apartine acestui bon");
        }

        return plata;
    }

    public Plata updatePlata(Long bonId, Long plataId, ro.facultate.pos.dto.UpdatePlataRequest req) {
        Plata plata = getPlata(bonId, plataId);

        if (plata.getStatus() == StatusPlata.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nu se poate modifica o plata cu status SUCCESS");
        }

        plata.setTip(req.getTip());
        plata.setSuma(req.getSuma());

        return plataRepository.save(plata);
    }

    public void deletePlata(Long bonId, Long plataId) {
        Plata plata = getPlata(bonId, plataId);

        if (plata.getStatus() == StatusPlata.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nu se poate sterge o plata cu status SUCCESS");
        }

        plataRepository.delete(plata);
    }
}
