package cl.proyectoEcommerce.ms_auditoria.controllers;

import cl.proyectoEcommerce.ms_auditoria.dto.RegistroAuditoriaResponseDTO;
import cl.proyectoEcommerce.ms_auditoria.models.RegistroAuditoria;
import cl.proyectoEcommerce.ms_auditoria.repositories.RegistroAuditoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {

    private final RegistroAuditoriaRepository registroAuditoriaRepository;

    public AuditoriaController(RegistroAuditoriaRepository registroAuditoriaRepository) {
        this.registroAuditoriaRepository = registroAuditoriaRepository;
    }

    // GET /api/v1/auditoria -> traza completa de eventos de RabbitMQ recibidos por el sistema,
    // usado como evidencia de que el nuevo microservicio consume las colas del ecosistema.
    @GetMapping
    public ResponseEntity<List<RegistroAuditoriaResponseDTO>> obtenerRegistros() {
        List<RegistroAuditoriaResponseDTO> registros = registroAuditoriaRepository.findAll().stream()
                .sorted(Comparator.comparing(RegistroAuditoria::getRecibidoEn).reversed())
                .map(r -> new RegistroAuditoriaResponseDTO(r.getId(), r.getTipoEvento(), r.getPayload(), r.getRecibidoEn()))
                .toList();
        return ResponseEntity.ok(registros);
    }
}
