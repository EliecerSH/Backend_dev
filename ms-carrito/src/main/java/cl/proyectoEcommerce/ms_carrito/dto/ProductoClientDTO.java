package cl.proyectoEcommerce.ms_carrito.dto;

import java.math.BigDecimal;

public record ProductoClientDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String categoria,
        String imagenUrl
) {}
