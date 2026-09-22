package cl.ProyectoEcommerce.ms_ordenes.exceptions;

public class OrdenNotFoundException extends RuntimeException {
    public OrdenNotFoundException(Long id) {
        super("La orden con ID " + id + " no existe.");
    }
}
