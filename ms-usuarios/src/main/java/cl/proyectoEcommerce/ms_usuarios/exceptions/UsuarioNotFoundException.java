package cl.proyectoEcommerce.ms_usuarios.exceptions;

public class UsuarioNotFoundException extends RuntimeException {

    public UsuarioNotFoundException(Integer id) {
        super("No se encontró el usuario con id: " + id);
    }

    public UsuarioNotFoundException(String mensaje) {
        super(mensaje);
    }
}