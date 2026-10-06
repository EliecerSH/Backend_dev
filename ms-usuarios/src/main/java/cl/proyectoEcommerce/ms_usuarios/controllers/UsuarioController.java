package cl.proyectoEcommerce.ms_usuarios.controllers;

import cl.proyectoEcommerce.ms_usuarios.models.Usuario;
import cl.proyectoEcommerce.ms_usuarios.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    // hasRole('Admin') busca la autoridad "ROLE_Admin"
    private static final String ROL_ADMIN = "ROLE_Admin";

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('Admin', 'User')")
    public ResponseEntity<?> obtenerUsuarioAutenticado(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(jwt.getClaims());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'User')")
    public ResponseEntity<Usuario> registrarUsuario(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Usuario usuario) {

        usuario.setId(null); // evita sobrescribir un usuario existente
        usuario.setAzureOid(jwt.getClaimAsString("oid"));
        usuario.setRol("User"); // el rol nunca viene del cliente

        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or @usuarioSecurity.esPropietario(#id, authentication)")
    public ResponseEntity<?> modificarUsuario(
            Authentication authentication,
            @PathVariable Integer id,
            @RequestBody Usuario usuario) {

        // Un User no puede cambiarse el rol al editar su perfil
        if (!esAdmin(authentication)) {
            usuario.setRol(usuarioService.obtenerUsuario(id).getRol());
        }

        Usuario usuarioActualizado = usuarioService.modificarUsuario(id, usuario);
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or @usuarioSecurity.esPropietario(#id, authentication)")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin') or @usuarioSecurity.esPropietario(#id, authentication)")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Integer id) {
        return new ResponseEntity<>(usuarioService.obtenerUsuario(id), HttpStatus.OK);
    }

    // Solo Admin puede listar a todos
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<Usuario>> obtenerTodos() {
        return new ResponseEntity<>(usuarioService.obtenerTodosLosUsuarios(), HttpStatus.OK);
    }

    // --- Helper ---

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROL_ADMIN));
    }
}
