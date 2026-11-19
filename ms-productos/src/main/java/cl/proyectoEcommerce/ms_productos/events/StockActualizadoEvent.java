package cl.proyectoEcommerce.ms_productos.events;

import java.time.LocalDateTime;

// Payload de auditoría del descuento de stock por una orden pagada
public record StockActualizadoEvent(
        Long ordenId,
        Long productoId,
        Integer cantidad,
        Integer stockAnterior,
        Integer stockNuevo,
        String detalle,
        LocalDateTime fecha
) {}
