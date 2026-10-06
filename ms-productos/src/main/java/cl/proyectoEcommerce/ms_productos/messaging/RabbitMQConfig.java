package cl.proyectoEcommerce.ms_productos.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.exchange}")
    private String exchangeName;

    @Value("${pedidos360.queue.stock}")
    private String stockQueueName;

    @Value("${pedidos360.routing-key.orden-creada}")
    private String routingKeyOrdenCreada;

    @Bean
    public TopicExchange pedidos360Exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue stockQueue() {
        return new Queue(stockQueueName, true);
    }

    @Bean
    public Binding stockBinding(
            Queue stockQueue,
            TopicExchange pedidos360Exchange) {

        return BindingBuilder
                .bind(stockQueue)
                .to(pedidos360Exchange)
                .with(routingKeyOrdenCreada);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
