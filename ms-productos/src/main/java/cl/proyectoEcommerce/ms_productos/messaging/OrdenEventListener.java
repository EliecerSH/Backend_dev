package cl.proyectoEcommerce.ms_productos.messaging;

import cl.proyectoEcommerce.ms_productos.events.OrdenCreadaEvent;
import cl.proyectoEcommerce.ms_productos.services.ProductoService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrdenEventListener {

    private final ProductoService productoService;

    public OrdenEventListener(ProductoService productoService) {
        this.productoService = productoService;
    }

    @RabbitListener(queues = "${pedidos360.queue.stock}")
    public void procesarOrdenCreada(OrdenCreadaEvent evento) {

        System.out.println(
                "Evento orden.creada recibido. Orden: "
                        + evento.ordenId()
        );

        evento.items().forEach(item -> {

            productoService.descontarStock(
                    item.productoId(),
                    item.cantidad()
            );

        });
    }
}
