package cl.proyectoEcommerce.ms_carrito.controllers;

import cl.proyectoEcommerce.ms_carrito.dto.AgregarItemRequestDTO;
import cl.proyectoEcommerce.ms_carrito.dto.CarritoResponseDTO;
import cl.proyectoEcommerce.ms_carrito.services.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carrito")
@PreAuthorize("hasAnyRole('Admin', 'User')")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(@AuthenticationPrincipal Jwt jwt) {
        String usuarioOid = extraerUsuarioOid(jwt);
        return ResponseEntity.ok(carritoService.obtenerOcrearCarrito(usuarioOid));
    }

    @PostMapping("/items")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AgregarItemRequestDTO request) {
        String usuarioOid = extraerUsuarioOid(jwt);
        return ResponseEntity.ok(carritoService.agregarProducto(usuarioOid, request));
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<CarritoResponseDTO> eliminarProducto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productoId) {
        String usuarioOid = extraerUsuarioOid(jwt);
        return ResponseEntity.ok(carritoService.eliminarProducto(usuarioOid, productoId));
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal Jwt jwt) {
        String usuarioOid = extraerUsuarioOid(jwt);
        carritoService.vaciarCarrito(usuarioOid);
        return ResponseEntity.noContent().build();
    }

    private String extraerUsuarioOid(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        if (oid != null && !oid.isBlank()) {
            return oid;
        }
        return jwt.getSubject();
    }
}