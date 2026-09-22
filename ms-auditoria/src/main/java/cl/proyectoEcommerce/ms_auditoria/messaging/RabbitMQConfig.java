package cl.proyectoEcommerce.ms_auditoria.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.exchange}")
    private String exchangeName;

    @Value("${pedidos360.queue.auditoria}")
    private String queueName;

    @Value("${pedidos360.routing-key.todos-los-eventos}")
    private String routingKeyTodos;

    @Bean
    public TopicExchange pedidos360Exchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // Cola de auditoría: se enlaza con "#" para capturar cualquier evento publicado
    // en el exchange del sistema (orden.creada, stock.actualizado, etc.)
    @Bean
    public Queue auditoriaQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding auditoriaBinding(Queue auditoriaQueue, TopicExchange pedidos360Exchange) {
        return BindingBuilder.bind(auditoriaQueue).to(pedidos360Exchange).with(routingKeyTodos);
    }

    // Nota: a propósito NO se registra un Jackson2JsonMessageConverter global aquí.
    // El listener trabaja con el mensaje crudo (org.springframework.amqp.core.Message)
    // para poder auditar cualquier tipo de evento sin acoplarse a un DTO específico.
}
