package cl.proyectoEcommerce.ms_carrito.clients;

import cl.proyectoEcommerce.ms_carrito.dto.ProductoClientDTO;
import cl.proyectoEcommerce.ms_carrito.exceptions.ProductoNoEncontradoException;
import cl.proyectoEcommerce.ms_carrito.exceptions.ServicioProductosNoDisponibleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductoClient {

    // OJO: este campo es el RestClient (para hacer HTTP), NO confundir con la clase ProductoClient
    private final RestClient restClient;

    public ProductoClient(
            RestClient.Builder restClientBuilder,
            @Value("${spring.productos.service.url}") String productosUrl) {
        this.restClient = restClientBuilder.baseUrl(productosUrl).build();
    }

    @CircuitBreaker(name = "msProductos", fallbackMethod = "fallbackConsultarProducto")
    public ProductoClientDTO consultarProducto(Long productoId) {
        return restClient.get()
                .uri("/api/v1/productos/{id}", productoId)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, resp) -> {
                    throw new ProductoNoEncontradoException(productoId);
                })
                .body(ProductoClientDTO.class);
    }

    public ProductoClientDTO fallbackConsultarProducto(Long productoId, Throwable t) {
        if (t instanceof ProductoNoEncontradoException) {
            throw (ProductoNoEncontradoException) t;
        }
        throw new ServicioProductosNoDisponibleException(
                "El catálogo de productos no está disponible en este momento. Intenta de nuevo más tarde."
        );
    }
}
