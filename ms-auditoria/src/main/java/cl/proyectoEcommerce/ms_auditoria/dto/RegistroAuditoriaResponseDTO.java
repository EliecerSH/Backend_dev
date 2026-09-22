package cl.proyectoEcommerce.ms_auditoria.dto;

import java.time.LocalDateTime;

public record RegistroAuditoriaResponseDTO(
        Long id,
        String tipoEvento,
        String payload,
        LocalDateTime recibidoEn
) {}
