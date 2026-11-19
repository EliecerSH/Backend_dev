package cl.proyectoEcommerce.ms_productos.messaging;

import cl.proyectoEcommerce.ms_productos.events.OrdenCreadaEvent;
import cl.proyectoEcommerce.ms_productos.exceptions.ProductoNotFoundException;
import cl.proyectoEcommerce.ms_productos.exceptions.StockInsuficienteException;
import cl.proyectoEcommerce.ms_productos.services.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

// Consume "orden.creada" desde RabbitMQ (cola pedidos360.stock.queue) y descuenta el stock.
@Component
public class OrdenEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenEventListener.class);

    private final ProductoService productoService;

    public OrdenEventListener(ProductoService productoService) {
        this.productoService = productoService;
    }

    @RabbitListener(queues = "${pedidos360.queue.stock}")
    public void procesarOrdenCreada(OrdenCreadaEvent evento) {
        log.info("Evento orden.creada recibido. Orden: {}", evento.ordenId());
        try {
            productoService.descontarStockOrden(evento);
        } catch (StockInsuficienteException | ProductoNotFoundException e) {
            // Error de negocio: reintentar no lo arregla. Se da el mensaje por procesado (antes quedaba en un
            // ciclo infinito de reintentos) y se deja registro en auditoría para revisión manual.
            // La transacción se revirtió completa, así que ningún producto de la orden quedó descontado.
            log.error("No se pudo descontar el stock de la orden {}: {}", evento.ordenId(), e.getMessage());
            productoService.auditarErrorStock(evento, e.getMessage());
        }
        // Errores técnicos (BD caída, etc.) se propagan: se reintenta 3 veces y luego se descarta
        // sin reencolar (ver spring.rabbitmq.listener en application.yml)
    }
}
