package cl.proyectoEcommerce.ms_carrito.exceptions;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(Long id) {
        super("El producto con ID " + id + " no existe en el catálogo.");
    }
}