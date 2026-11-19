package cl.proyectoEcommerce.ms_productos.exceptions;

public class StockInsuficienteException extends RuntimeException {
    public StockInsuficienteException(Long productoId, int disponible, int solicitado) {
        super("Stock insuficiente para el producto " + productoId
                + " (disponible " + disponible + ", solicitado " + solicitado + ")");
    }
}
