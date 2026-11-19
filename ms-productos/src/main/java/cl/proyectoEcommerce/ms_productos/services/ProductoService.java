package cl.proyectoEcommerce.ms_productos.services;

import cl.proyectoEcommerce.ms_productos.dto.ProductoRequestDTO;
import cl.proyectoEcommerce.ms_productos.dto.ProductoResponseDTO;
import cl.proyectoEcommerce.ms_productos.events.ItemEvento;
import cl.proyectoEcommerce.ms_productos.events.OrdenCreadaEvent;
import cl.proyectoEcommerce.ms_productos.events.ProductoModificadoEvent;
import cl.proyectoEcommerce.ms_productos.events.StockActualizadoEvent;
import cl.proyectoEcommerce.ms_productos.exceptions.ProductoNotFoundException;
import cl.proyectoEcommerce.ms_productos.exceptions.StockInsuficienteException;
import cl.proyectoEcommerce.ms_productos.messaging.ProductoAuditPublisher;
import cl.proyectoEcommerce.ms_productos.models.Producto;
import cl.proyectoEcommerce.ms_productos.repositories.ProductoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final ProductoAuditPublisher productoAuditPublisher;

    // Inyección de dependencias por constructor sin Lombok
    public ProductoService(
            ProductoRepository productoRepository,
            ProductoAuditPublisher productoAuditPublisher) {

        this.productoRepository = productoRepository;
        this.productoAuditPublisher = productoAuditPublisher;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {

        return productoRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorId(Long id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));

        return mapToResponseDTO(producto);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerPorCategoria(String categoria) {

        return productoRepository.findByCategoriaIgnoreCase(categoria).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO requestDTO) {
        return crearProducto(requestDTO, null);
    }

    @Transactional
    public ProductoResponseDTO crearProducto(ProductoRequestDTO requestDTO, String usuarioOid) {

        Producto producto = new Producto();

        producto.setNombre(requestDTO.nombre());
        producto.setDescripcion(requestDTO.descripcion());
        producto.setPrecio(requestDTO.precio());
        producto.setStock(requestDTO.stock());
        producto.setCategoria(requestDTO.categoria());
        producto.setImagenUrl(requestDTO.imagenUrl());

        Producto productoGuardado = productoRepository.save(producto);

        auditarProducto(ProductoAuditPublisher.PRODUCTO_CREADO, productoGuardado, usuarioOid);
        return mapToResponseDTO(productoGuardado);
    }

    @Transactional
    public ProductoResponseDTO actualizarProducto(
            Long id,
            ProductoRequestDTO requestDTO,
            String usuarioOid) {

        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));

        // Actualizar producto
        productoExistente.setNombre(requestDTO.nombre());
        productoExistente.setDescripcion(requestDTO.descripcion());
        productoExistente.setPrecio(requestDTO.precio());
        productoExistente.setStock(requestDTO.stock());
        productoExistente.setCategoria(requestDTO.categoria());
        productoExistente.setImagenUrl(requestDTO.imagenUrl());

        Producto productoActualizado =
                productoRepository.save(productoExistente);

        // Auditoría por Kafka (tópico auditoria.eventos), después del commit
        auditarProducto(ProductoAuditPublisher.PRODUCTO_MODIFICADO, productoActualizado, usuarioOid);
        log.info("Producto {} modificado por {}", productoActualizado.getId(), usuarioOid);

        return mapToResponseDTO(productoActualizado);
    }

    @Transactional
    public void eliminarProducto(Long id) {
        eliminarProducto(id, null);
    }

    @Transactional
    public void eliminarProducto(Long id, String usuarioOid) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));

        productoRepository.delete(producto);
        auditarProducto(ProductoAuditPublisher.PRODUCTO_ELIMINADO, producto, usuarioOid);
    }

    private void auditarProducto(String tipo, Producto producto, String usuarioOid) {
        productoAuditPublisher.publicar(tipo, producto.getId(), new ProductoModificadoEvent(
                producto.getId(),
                usuarioOid,
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                LocalDateTime.now()
        ));
    }

    // Mapeador privado de Entidad -> ResponseDTO (Record)
    private ProductoResponseDTO mapToResponseDTO(Producto producto) {

        return new ProductoResponseDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoria(),
                producto.getImagenUrl(),
                producto.getCreadoEn()
        );
    }

    // Descuenta el stock de TODOS los ítems de una orden en una sola transacción (todo o nada).
    // Antes se descontaba ítem por ítem fuera de una transacción común: si el 2° producto no tenía stock,
    // el 1° ya estaba descontado, RabbitMQ reintentaba el mensaje y lo volvía a descontar en cada reintento.
    @Transactional
    public void descontarStockOrden(OrdenCreadaEvent evento) {
        List<StockActualizadoEvent> cambios = new java.util.ArrayList<>();
        for (ItemEvento item : evento.items()) {
            Producto producto = productoRepository.findById(item.productoId())
                    .orElseThrow(() -> new ProductoNotFoundException(item.productoId()));
            int stockAnterior = producto.getStock();
            if (stockAnterior < item.cantidad()) {
                throw new StockInsuficienteException(item.productoId(), stockAnterior, item.cantidad());
            }
            producto.setStock(stockAnterior - item.cantidad());
            productoRepository.save(producto);
            cambios.add(new StockActualizadoEvent(evento.ordenId(), item.productoId(), item.cantidad(),
                    stockAnterior, producto.getStock(), null, LocalDateTime.now()));
            log.info("Stock descontado: orden={} producto={} cantidad={} stock {} -> {}",
                    evento.ordenId(), item.productoId(), item.cantidad(), stockAnterior, producto.getStock());
        }
        cambios.forEach(c -> productoAuditPublisher.publicar(ProductoAuditPublisher.STOCK_ACTUALIZADO, c.productoId(), c));
    }

    // Registra en auditoría que el stock de una orden NO se pudo descontar (requiere revisión manual)
    public void auditarErrorStock(OrdenCreadaEvent evento, String motivo) {
        productoAuditPublisher.publicar(ProductoAuditPublisher.STOCK_ERROR, evento.ordenId(),
                new StockActualizadoEvent(evento.ordenId(), null, null, null, null, motivo, LocalDateTime.now()));
    }

    // Se mantiene por compatibilidad: descuenta un solo producto en su propia transacción
    @Transactional
    public void descontarStock(Long productoId, Integer cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        if (producto.getStock() < cantidad) {
            throw new StockInsuficienteException(productoId, producto.getStock(), cantidad);
        }
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }
}
