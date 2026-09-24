package cl.proyectoEcommerce.ms_carrito.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponseDTO(
        Long id,
        String usuarioOid,
        List<CarritoItemResponseDTO> items,
        BigDecimal total
) {}
