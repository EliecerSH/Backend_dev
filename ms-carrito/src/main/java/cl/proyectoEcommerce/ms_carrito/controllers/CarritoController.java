package cl.proyectoEcommerce.ms_carrito.controllers;

import cl.proyectoEcommerce.ms_carrito.dto.AgregarItemRequestDTO;
import cl.proyectoEcommerce.ms_carrito.dto.CarritoResponseDTO;
import cl.proyectoEcommerce.ms_carrito.services.CarritoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    // Inyección de dependencias por constructor sin Lombok
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // GET /api/v1/carrito -> Obtener el carrito del usuario autenticado
    @GetMapping
    public ResponseEntity<CarritoResponseDTO> obtenerCarrito(@AuthenticationPrincipal Jwt jwt) {
        String usuarioOid = extraerUsuarioOid(jwt);
        CarritoResponseDTO carrito = carritoService.obtenerOcrearCarrito(usuarioOid);
        return ResponseEntity.ok(carrito);
    }

    // POST /api/v1/carrito/items -> Agregar o actualizar la cantidad de un producto
    @PostMapping("/items")
    public ResponseEntity<CarritoResponseDTO> agregarProducto(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AgregarItemRequestDTO request) {
        String usuarioOid = extraerUsuarioOid(jwt);
        CarritoResponseDTO carrito = carritoService.agregarProducto(usuarioOid, request);
        return ResponseEntity.ok(carrito);
    }

    // DELETE /api/v1/carrito/items/{productoId} -> Remover un producto específico
    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<CarritoResponseDTO> eliminarProducto(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productoId) {
        String usuarioOid = extraerUsuarioOid(jwt);
        CarritoResponseDTO carrito = carritoService.eliminarProducto(usuarioOid, productoId);
        return ResponseEntity.ok(carrito);
    }

    // DELETE /api/v1/carrito -> Vaciar el carrito completamente
    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito(@AuthenticationPrincipal Jwt jwt) {
        String usuarioOid = extraerUsuarioOid(jwt);
        carritoService.vaciarCarrito(usuarioOid);
        return ResponseEntity.noContent().build();
    }

    // Método privado para extraer el OID (o fallback al 'sub' del token JWT de Azure)
    private String extraerUsuarioOid(Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        if (oid != null && !oid.isBlank()) {
            return oid;
        }
        // Fallback al claim 'sub' si 'oid' no viene en tokens client_credentials
        return jwt.getSubject();
    }
}
