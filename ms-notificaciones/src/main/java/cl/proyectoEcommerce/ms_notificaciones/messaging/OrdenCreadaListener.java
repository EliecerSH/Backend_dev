package cl.proyectoEcommerce.ms_notificaciones.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import cl.proyectoEcommerce.ms_notificaciones.events.OrdenCreadaEvent;
import cl.proyectoEcommerce.ms_notificaciones.services.EmailService;

@Component
public class OrdenCreadaListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenCreadaListener.class);

    private final EmailService emailService;

    public OrdenCreadaListener(EmailService emailService) {
        this.emailService = emailService;
    }

    // Consume el evento "orden.creada" publicado por ms-ordenes y envía la
    // notificación de confirmación de compra por correo electrónico.
    @RabbitListener(queues = "${pedidos360.queue.notificaciones}")
    public void manejarOrdenCreada(OrdenCreadaEvent evento) {
        log.info("Evento orden.creada recibido para la orden #{} del usuario {}", evento.ordenId(),
                evento.usuarioOid());

        String asunto = "Pedidos360 - Confirmación de compra #" + evento.ordenId();
        String cuerpo = "Tu orden #" + evento.ordenId() + " fue registrada correctamente.\n" +
                "Total: $" + evento.total() + "\n" +
                "Ítems: " + evento.items().size() + "\n" +
                "Fecha: " + evento.fecha();

        emailService.enviarNotificacionOrden(evento.usuarioOid(), asunto, cuerpo);
    }
}
