package cl.ProyectoEcommerce.ms_ordenes.messaging;

import cl.ProyectoEcommerce.ms_ordenes.events.OrdenCreadaEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrdenEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKeyOrdenCreada;

    public OrdenEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${pedidos360.exchange}") String exchangeName,
            @Value("${pedidos360.routing-key.orden-creada}") String routingKeyOrdenCreada) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKeyOrdenCreada = routingKeyOrdenCreada;
    }

    public void publicarOrdenCreada(OrdenCreadaEvent evento) {
        rabbitTemplate.convertAndSend(exchangeName, routingKeyOrdenCreada, evento);
    }
}
