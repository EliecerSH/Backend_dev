package cl.proyectoEcommerce.ms_usuarios.controllers;

import cl.proyectoEcommerce.ms_usuarios.models.Usuario;
import cl.proyectoEcommerce.ms_usuarios.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioAutenticado(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jwt.getClaims());
    }

    @PostMapping
    public ResponseEntity<Usuario> registrarUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Usuario usuario) {

        String azureOid = jwt.getClaimAsString("oid");
        usuario.setAzureOid(azureOid);
        // Punto 2: el rol NUNCA viene del cliente, se fuerza aquí
        usuario.setRol("CLIENTE");

        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @RequestBody Usuario usuario) {

        Usuario objetivo = usuarioService.obtenerUsuario(id);
        if (!esPropioOAdmin(jwt, objetivo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para modificar este usuario.");
        }

        // Un CLIENTE no puede auto-asignarse otro rol al editar su perfil
        if (!esAdmin(jwt)) {
            usuario.setRol(objetivo.getRol());
        }

        Usuario usuarioActualizado = usuarioService.modificarUsuario(id, usuario);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {

        Usuario objetivo = usuarioService.obtenerUsuario(id);
        if (!esPropioOAdmin(jwt, objetivo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para eliminar este usuario.");
        }

        usuarioService.eliminarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {

        Usuario usuario = usuarioService.obtenerUsuario(id);
        if (!esPropioOAdmin(jwt, usuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para ver este usuario.");
        }

        return new ResponseEntity<>(usuario, HttpStatus.OK);
    }

    // Solo ADMIN puede listar a todos
    @GetMapping
    public ResponseEntity<?> obtenerTodos(@AuthenticationPrincipal Jwt jwt) {
        if (!esAdmin(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo un administrador puede listar todos los usuarios.");
        }
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    // --- Helpers de autorización ---

    private boolean esPropioOAdmin(Jwt jwt, Usuario objetivo) {
        String oid = jwt.getClaimAsString("oid");
        boolean esDueño = oid != null && oid.equals(objetivo.getAzureOid());
        return esDueño || esAdmin(jwt);
    }

    private boolean esAdmin(Jwt jwt) {
        // Ajusta esto a como realmente representes roles en tu sistema.
        // Opción simple: mirar el rol guardado en tu BD, no en el JWT
        // (más abajo te explico por qué). Placeholder aquí:
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }
}
