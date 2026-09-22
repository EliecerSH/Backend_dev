package cl.ProyectoEcommerce.ms_ordenes.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Evento publicado a RabbitMQ cuando se registra una nueva orden.
// Lo consumen: ms-productos (descuenta stock), ms-notificaciones (envía correo)
// y ms-auditoria (deja registro del evento).
public record OrdenCreadaEvent(
        Long ordenId,
        String usuarioOid,
        List<ItemEvento> items,
        BigDecimal total,
        LocalDateTime fecha) implements Serializable {
}
