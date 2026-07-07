package ro.facultate.pos.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ro.facultate.pos.event.BonPlatitEvent;

@Component
public class NotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificationEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBonPlatit(BonPlatitEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY_BON_PLATIT, event);
        log.debug("Eveniment bon.platit publicat pentru bonul {}", event.getBonId());
    }
}
