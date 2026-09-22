package cl.ProyectoEcommerce.ms_ordenes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponseDTO(
        Long id,
        String usuarioOid,
        List<ItemOrdenResponseDTO> items,
        BigDecimal total,
        String estado,
        LocalDateTime creadoEn) {
}
