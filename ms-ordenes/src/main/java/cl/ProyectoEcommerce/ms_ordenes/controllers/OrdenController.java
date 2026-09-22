package cl.ProyectoEcommerce.ms_ordenes.controllers;

import cl.ProyectoEcommerce.ms_ordenes.dto.CrearOrdenRequestDTO;
import cl.ProyectoEcommerce.ms_ordenes.dto.OrdenResponseDTO;
import cl.ProyectoEcommerce.ms_ordenes.services.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    // POST /api/v1/ordenes -> registra una orden formal a partir del carrito y
    // dispara el
    // flujo asíncrono (descuento de stock + notificación por correo + auditoría)
    @PostMapping
    public ResponseEntity<OrdenResponseDTO> crearOrden(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CrearOrdenRequestDTO request) {
        String usuarioOid = extraerUsuarioOid(jwt);
        OrdenResponseDTO orden = ordenService.crearOrden(usuarioOid, request);
        return new ResponseEntity<>(orden, HttpStatus.CREATED);
    }

    // GET /api/v1/ordenes -> historial de compras del usuario autenticado
    @GetMapping
    public ResponseEntity<List<OrdenResponseDTO>> obtenerMisOrdenes(@AuthenticationPrincipal Jwt jwt) {
        String usuarioOid = extraerUsuarioOid(jwt);
        return ResponseEntity.ok(ordenService.obtenerOrdenesDeUsuario(usuarioOid));
    }

    // GET /api/v1/ordenes/{id} -> detalle de una orden puntual
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerPorId(id));
    }

    private String extraerUsuarioOid(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        if (oid != null && !oid.isBlank()) {
            return oid;
        }
        return jwt.getSubject();
    }
}
