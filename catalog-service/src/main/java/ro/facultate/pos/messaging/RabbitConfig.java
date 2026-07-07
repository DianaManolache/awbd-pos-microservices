package ro.facultate.pos.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Catalog-service e producator pe exchange-ul de evenimente - declara doar
 * exchange-ul (idempotent), nu cozi/bindinguri; consumatorul
 * (notification-service) detine topologia completa.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pos.events";
    public static final String ROUTING_KEY_STOC_EPUIZAT = "produs.stoc-epuizat";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
