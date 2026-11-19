package cl.proyectoEcommerce.ms_usuarios.exceptions;

public class UsuarioYaRegistradoException extends RuntimeException {
    public UsuarioYaRegistradoException() {
        super("Ya tienes un perfil registrado. Puedes editarlo desde \"Mi cuenta\".");
    }
}
