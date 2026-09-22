package cl.ProyectoEcommerce.ms_ordenes.services;

import cl.ProyectoEcommerce.ms_ordenes.dto.*;
import cl.ProyectoEcommerce.ms_ordenes.events.ItemEvento;
import cl.ProyectoEcommerce.ms_ordenes.events.OrdenCreadaEvent;
import cl.ProyectoEcommerce.ms_ordenes.exceptions.OrdenNotFoundException;
import cl.ProyectoEcommerce.ms_ordenes.messaging.OrdenEventPublisher;
import cl.ProyectoEcommerce.ms_ordenes.models.Orden;
import cl.ProyectoEcommerce.ms_ordenes.models.OrdenItem;
import cl.ProyectoEcommerce.ms_ordenes.repositories.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrdenService {

        private final OrdenRepository ordenRepository;
        private final OrdenEventPublisher ordenEventPublisher;

        public OrdenService(OrdenRepository ordenRepository, OrdenEventPublisher ordenEventPublisher) {
                this.ordenRepository = ordenRepository;
                this.ordenEventPublisher = ordenEventPublisher;
        }

        // Registra la orden a partir del snapshot del carrito recibido desde el
        // frontend,
        // la persiste como PROCESADA y publica el evento "orden.creada" en RabbitMQ
        // para que
        // ms-productos descuente el stock, ms-notificaciones envíe el correo y
        // ms-auditoria
        // deje registro del movimiento.
        @Transactional
        public OrdenResponseDTO crearOrden(String usuarioOid, CrearOrdenRequestDTO request) {
                BigDecimal total = request.items().stream()
                                .map(item -> item.precioUnitario().multiply(BigDecimal.valueOf(item.cantidad())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                Orden orden = new Orden(usuarioOid, total);
                request.items().forEach(item -> orden
                                .agregarItem(new OrdenItem(item.productoId(), item.cantidad(), item.precioUnitario())));
                orden.setEstado("PROCESADA");

                Orden ordenGuardada = ordenRepository.save(orden);

                List<ItemEvento> itemsEvento = ordenGuardada.getItems().stream()
                                .map(i -> new ItemEvento(i.getProductoId(), i.getCantidad(), i.getPrecioUnitario()))
                                .toList();

                ordenEventPublisher.publicarOrdenCreada(new OrdenCreadaEvent(
                                ordenGuardada.getId(),
                                usuarioOid,
                                itemsEvento,
                                ordenGuardada.getTotal(),
                                LocalDateTime.now()));

                return mapToResponseDTO(ordenGuardada);
        }

        @Transactional(readOnly = true)
        public List<OrdenResponseDTO> obtenerOrdenesDeUsuario(String usuarioOid) {
                return ordenRepository.findByUsuarioOidOrderByCreadoEnDesc(usuarioOid).stream()
                                .map(this::mapToResponseDTO)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public OrdenResponseDTO obtenerPorId(Long id) {
                Orden orden = ordenRepository.findById(id)
                                .orElseThrow(() -> new OrdenNotFoundException(id));
                return mapToResponseDTO(orden);
        }

        private OrdenResponseDTO mapToResponseDTO(Orden orden) {
                List<ItemOrdenResponseDTO> items = orden.getItems().stream()
                                .map(i -> new ItemOrdenResponseDTO(i.getProductoId(), i.getCantidad(),
                                                i.getPrecioUnitario(), i.getSubtotal()))
                                .toList();

                return new OrdenResponseDTO(
                                orden.getId(),
                                orden.getUsuarioOid(),
                                items,
                                orden.getTotal(),
                                orden.getEstado(),
                                orden.getCreadoEn());
        }
}
