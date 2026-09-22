package cl.proyectoEcommerce.ms_notificaciones.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.exchange}")
    private String exchangeName;

    @Value("${pedidos360.queue.notificaciones}")
    private String queueName;

    @Value("${pedidos360.routing-key.orden-creada}")
    private String routingKeyOrdenCreada;

    @Bean
    public TopicExchange pedidos360Exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // Cola propia y duradera: si ms-notificaciones está caído, RabbitMQ retiene los
    // eventos "orden.creada" hasta que vuelva a levantarse
    @Bean
    public Queue notificacionesQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding notificacionesBinding(Queue notificacionesQueue, TopicExchange pedidos360Exchange) {
        return BindingBuilder.bind(notificacionesQueue).to(pedidos360Exchange).with(routingKeyOrdenCreada);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
