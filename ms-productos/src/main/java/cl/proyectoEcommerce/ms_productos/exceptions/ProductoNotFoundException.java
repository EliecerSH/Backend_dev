package cl.proyectoEcommerce.ms_productos.exceptions;

public class ProductoNotFoundException extends RuntimeException {
    public ProductoNotFoundException(Long id) {
        super("Producto no encontrado con el ID: " + id);
    }
}