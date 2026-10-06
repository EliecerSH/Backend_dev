package cl.proyectoEcommerce.ms_productos.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenCreadaEvent(
        Long ordenId,
        String usuarioOid,
        List<ItemEvento> items,
        BigDecimal total,
        LocalDateTime fecha
) {}
