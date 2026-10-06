package cl.proyectoEcommerce.ms_productos.messaging;

import cl.proyectoEcommerce.ms_productos.events.ProductoModificadoEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductoAuditPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;

    public ProductoAuditPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${pedidos360.exchange}") String exchangeName,
            @Value("${pedidos360.routing-key.producto-modificado}") String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    public void publicarProductoModificado(ProductoModificadoEvent evento) {

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                evento
        );
    }
}
