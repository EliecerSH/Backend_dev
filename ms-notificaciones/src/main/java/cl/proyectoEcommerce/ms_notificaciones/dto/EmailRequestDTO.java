package cl.proyectoEcommerce.ms_notificaciones.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Body para disparar manualmente un correo de prueba desde Postman
public record EmailRequestDTO(
                @NotBlank(message = "El destinatario es obligatorio") @Email(message = "El destinatario debe ser un correo válido") String destinatario,

                @NotBlank(message = "El asunto es obligatorio") String asunto,

                @NotBlank(message = "El mensaje es obligatorio") String mensaje) {
}
