package cl.proyectoEcommerce.ms_carrito.dto;

import java.math.BigDecimal;

public record CarritoItemResponseDTO(
        Long id,
        Long productoId,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
