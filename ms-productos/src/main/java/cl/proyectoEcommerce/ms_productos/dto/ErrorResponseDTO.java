package cl.proyectoEcommerce.ms_productos.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponseDTO(
        int status,
        String error,
        String mensaje,
        Map<String, String> detalles,
        LocalDateTime timestamp
) {
    // Constructor de conveniencia para errores simples sin mapa de detalles
    public ErrorResponseDTO(int status, String error, String mensaje) {
        this(status, error, mensaje, null, LocalDateTime.now());
    }

    // Constructor de conveniencia para errores con mapa de detalles (validaciones)
    public ErrorResponseDTO(int status, String error, String mensaje, Map<String, String> detalles) {
        this(status, error, mensaje, detalles, LocalDateTime.now());
    }
}