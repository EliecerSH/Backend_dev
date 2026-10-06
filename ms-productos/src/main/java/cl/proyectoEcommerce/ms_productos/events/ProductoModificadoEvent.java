package cl.proyectoEcommerce.ms_productos.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoModificadoEvent(
        Long productoId,
        String usuarioOid,
        String nombre,
        BigDecimal precio,
        Integer stock,
        LocalDateTime fecha
) {}