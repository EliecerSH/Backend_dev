package cl.proyectoEcommerce.ms_usuarios.services;

import cl.proyectoEcommerce.ms_usuarios.exceptions.UsuarioNotFoundException;
import cl.proyectoEcommerce.ms_usuarios.exceptions.UsuarioYaRegistradoException;
import cl.proyectoEcommerce.ms_usuarios.models.Usuario;
import cl.proyectoEcommerce.ms_usuarios.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        // azure_oid es único: antes un segundo registro terminaba en un 500 genérico
        if (usuario.getAzureOid() != null && usuarioRepository.existsByAzureOid(usuario.getAzureOid())) {
            throw new UsuarioYaRegistradoException();
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario modificarUsuario(Integer id, Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setDireccion(usuario.getDireccion());
        usuarioExistente.setComuna(usuario.getComuna());
        usuarioExistente.setRegion(usuario.getRegion());
        usuarioExistente.setCiudad(usuario.getCiudad());
        // Si el body no trae rol (el formulario de "Mi cuenta" no lo envía), se conserva el actual.
        // Antes se guardaba null y un Admin que editaba su perfil perdía el rol.
        if (usuario.getRol() != null && !usuario.getRol().isBlank()) {
            usuarioExistente.setRol(usuario.getRol());
        }

        return usuarioRepository.save(usuarioExistente);
    }

    @Override
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNotFoundException(id);
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public Usuario obtenerUsuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }

    @Override
    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario obtenerPorAzureOid(String azureOid) {
        return usuarioRepository.findByAzureOid(azureOid)
                .orElseThrow(() -> new UsuarioNotFoundException("No existe un perfil registrado para este usuario."));
    }
}
