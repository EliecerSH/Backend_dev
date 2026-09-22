package cl.ProyectoEcommerce.ms_ordenes.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.exchange}")
    private String exchangeName;

    // Exchange de tipo topic: permite enrutar eventos como "orden.creada",
    // "stock.actualizado", etc.
    @Bean
    public TopicExchange pedidos360Exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // Serializa/deserializa los mensajes como JSON en vez de Java Serializable
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
