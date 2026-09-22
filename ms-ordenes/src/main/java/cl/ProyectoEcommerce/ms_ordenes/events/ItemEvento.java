package cl.ProyectoEcommerce.ms_ordenes.events;

import java.math.BigDecimal;

public record ItemEvento(Long productoId, Integer cantidad, BigDecimal precioUnitario) {
}
