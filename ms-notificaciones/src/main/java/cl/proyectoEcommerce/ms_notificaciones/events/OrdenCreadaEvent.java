package cl.proyectoEcommerce.ms_notificaciones.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Mismo contrato de evento publicado por ms-ordenes en el exchange "pedidos360.exchange"
public record OrdenCreadaEvent(
                Long ordenId,
                String usuarioOid,
                List<ItemEvento> items,
                BigDecimal total,
                LocalDateTime fecha) implements Serializable {
}
