package cl.proyectoEcommerce.ms_notificaciones.events;

import java.math.BigDecimal;

public record ItemEvento(Long productoId, Integer cantidad, BigDecimal precioUnitario) {
}
