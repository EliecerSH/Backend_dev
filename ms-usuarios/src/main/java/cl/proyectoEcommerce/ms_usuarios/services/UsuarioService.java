package cl.proyectoEcommerce.ms_usuarios.services;

import cl.proyectoEcommerce.ms_usuarios.models.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario registrarUsuario(Usuario usuario);

    Usuario modificarUsuario(Integer id, Usuario usuario);

    void eliminarUsuario(Integer id);

    Usuario obtenerUsuario(Integer id);

    List<Usuario> obtenerTodosLosUsuarios();
}
