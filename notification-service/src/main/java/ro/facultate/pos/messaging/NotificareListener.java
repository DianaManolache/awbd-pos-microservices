package ro.facultate.pos.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ro.facultate.pos.event.BonPlatitEvent;
import ro.facultate.pos.event.StocEpuizatEvent;
import ro.facultate.pos.service.NotificareService;

import java.util.Map;

@Component
public class NotificareListener {

    private final NotificareService notificareService;

    public NotificareListener(NotificareService notificareService) {
        this.notificareService = notificareService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_BON_PLATIT)
    public void onBonPlatit(BonPlatitEvent event) {
        String mesaj = "Bon " + event.getBonId() + " platit (" + event.getTipPlata() + "), suma " + event.getTotal();
        notificareService.inregistreaza("BON_PLATIT", mesaj, Map.of(
                "bonId", event.getBonId(),
                "clientId", event.getClientId(),
                "vanzatorId", event.getVanzatorId(),
                "total", event.getTotal(),
                "tipPlata", event.getTipPlata()
        ));
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_STOC_EPUIZAT)
    public void onStocEpuizat(StocEpuizatEvent event) {
        String mesaj = "Produsul '" + event.getNume() + "' (id " + event.getProdusId() + ") a ramas fara stoc";
        notificareService.inregistreaza("STOC_EPUIZAT", mesaj, Map.of(
                "produsId", event.getProdusId(),
                "nume", event.getNume(),
                "stoc", event.getStoc()
        ));
    }
}
