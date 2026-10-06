package cl.proyectoEcommerce.ms_productos.events;

import java.math.BigDecimal;

public record ItemEvento(
        Long productoId,
        Integer cantidad,
        BigDecimal precioUnitario
) {}
