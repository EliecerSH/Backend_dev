package cl.proyectoEcommerce.ms_notificaciones.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.proyectoEcommerce.ms_notificaciones.dto.EmailRequestDTO;
import cl.proyectoEcommerce.ms_notificaciones.services.EmailService;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionController {

    private final EmailService emailService;

    public NotificacionController(EmailService emailService) {
        this.emailService = emailService;
    }

    // POST /api/v1/notificaciones/test-email -> dispara un correo directo, pensado
    // para probar el servicio de forma aislada desde Postman (ver enunciado 2,
    // punto 4).
    @PostMapping("/test-email")
    public ResponseEntity<String> enviarCorreoDePrueba(@Valid @RequestBody EmailRequestDTO request) {
        emailService.enviarCorreo(request.destinatario(), request.asunto(), request.mensaje());
        return ResponseEntity.ok("Notificación procesada para " + request.destinatario());
    }
}
