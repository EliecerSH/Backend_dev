package cl.ProyectoEcommerce.ms_ordenes.dto;

import java.math.BigDecimal;

public record ItemOrdenResponseDTO(
        Long productoId,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {
}
