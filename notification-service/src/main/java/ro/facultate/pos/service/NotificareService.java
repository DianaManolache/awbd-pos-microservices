package ro.facultate.pos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ro.facultate.pos.entity.Notificare;
import ro.facultate.pos.repository.NotificareRepository;

import java.time.Instant;
import java.util.Map;

@Service
public class NotificareService {

    private static final Logger log = LoggerFactory.getLogger(NotificareService.class);

    private final NotificareRepository notificareRepository;

    public NotificareService(NotificareRepository notificareRepository) {
        this.notificareRepository = notificareRepository;
    }

    public Notificare inregistreaza(String tip, String mesaj, Map<String, Object> detalii) {
        Notificare notificare = new Notificare(tip, mesaj, detalii, Instant.now());
        Notificare saved = notificareRepository.save(notificare);
        log.info("Notificare inregistrata: tip={} mesaj={}", tip, mesaj);
        return saved;
    }

    public Page<Notificare> getPage(Pageable pageable) {
        return notificareRepository.findAll(pageable);
    }
}
