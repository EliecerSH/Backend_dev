package cl.proyectoEcommerce.ms_productos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer stock,
        String categoria,
        String imagenUrl,
        LocalDateTime creadoEn
) {}
