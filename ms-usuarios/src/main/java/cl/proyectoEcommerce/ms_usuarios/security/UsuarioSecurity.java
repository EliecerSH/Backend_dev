package cl.proyectoEcommerce.ms_usuarios.security;

import cl.proyectoEcommerce.ms_usuarios.models.Usuario;
import cl.proyectoEcommerce.ms_usuarios.services.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("usuarioSecurity")
public class UsuarioSecurity {

    private final UsuarioService usuarioService;

    public UsuarioSecurity(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public boolean esPropietario(Integer id, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            return false;
        }
        String oid = jwt.getClaimAsString("oid");
        Usuario objetivo = usuarioService.obtenerUsuario(id);
        return oid != null && oid.equals(objetivo.getAzureOid());
    }
    
}
