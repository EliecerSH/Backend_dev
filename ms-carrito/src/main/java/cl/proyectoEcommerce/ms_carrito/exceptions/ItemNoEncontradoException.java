package cl.proyectoEcommerce.ms_carrito.exceptions;

public class ItemNoEncontradoException extends RuntimeException {
    public ItemNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
