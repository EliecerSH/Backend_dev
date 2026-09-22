package cl.proyectoEcommerce.ms_auditoria.messaging;

import cl.proyectoEcommerce.ms_auditoria.models.RegistroAuditoria;
import cl.proyectoEcommerce.ms_auditoria.repositories.RegistroAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EventoListener {

    private static final Logger log = LoggerFactory.getLogger(EventoListener.class);

    private final RegistroAuditoriaRepository registroAuditoriaRepository;

    public EventoListener(RegistroAuditoriaRepository registroAuditoriaRepository) {
        this.registroAuditoriaRepository = registroAuditoriaRepository;
    }

    // Recibe cualquier evento publicado en pedidos360.exchange (routing key "#") y
    // deja un registro persistente en la BD de auditoría, sin importar de qué
    // microservicio provenga ni qué forma tenga el payload.
    @RabbitListener(queues = "${pedidos360.queue.auditoria}")
    public void auditarEvento(Message mensaje) {
        String routingKey = mensaje.getMessageProperties().getReceivedRoutingKey();
        String payload = new String(mensaje.getBody(), StandardCharsets.UTF_8);

        log.info("[AUDITORÍA] Evento '{}' registrado: {}", routingKey, payload);
        registroAuditoriaRepository.save(new RegistroAuditoria(routingKey, payload));
    }
}
