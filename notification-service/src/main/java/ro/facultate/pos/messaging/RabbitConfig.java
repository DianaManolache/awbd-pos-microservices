package ro.facultate.pos.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia de mesagerie pentru evenimentele de business (plata bon, stoc
 * epuizat). notification-service e consumatorul, deci el declara cozile,
 * bindingurile si dead-letter-ul; producatorii (sales-service,
 * catalog-service) declara doar exchange-ul, idempotent, ca sa functioneze
 * indiferent de ordinea de pornire.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pos.events";
    public static final String ROUTING_KEY_BON_PLATIT = "bon.platit";
    public static final String ROUTING_KEY_STOC_EPUIZAT = "produs.stoc-epuizat";

    public static final String QUEUE_BON_PLATIT = "notificari.bon-platit";
    public static final String QUEUE_STOC_EPUIZAT = "notificari.stoc-epuizat";

    private static final String DLX = "pos.events.dlx";
    private static final String DLQ = "pos.events.dlq";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange deadLetterExchange() {
        return new FanoutExchange(DLX, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding deadLetterBinding(FanoutExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange);
    }

    @Bean
    public Queue bonPlatitQueue() {
        return QueueBuilder.durable(QUEUE_BON_PLATIT)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Binding bonPlatitBinding(Queue bonPlatitQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(bonPlatitQueue).to(eventsExchange).with(ROUTING_KEY_BON_PLATIT);
    }

    @Bean
    public Queue stocEpuizatQueue() {
        return QueueBuilder.durable(QUEUE_STOC_EPUIZAT)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    @Bean
    public Binding stocEpuizatBinding(Queue stocEpuizatQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(stocEpuizatQueue).to(eventsExchange).with(ROUTING_KEY_STOC_EPUIZAT);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter("ro.facultate.pos.event");
    }
}
