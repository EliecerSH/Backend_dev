package cl.proyectoEcommerce.ms_carrito.exceptions;

public class ServicioProductosNoDisponibleException extends RuntimeException {
    public ServicioProductosNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
