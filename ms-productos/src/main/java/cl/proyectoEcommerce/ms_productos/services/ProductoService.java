package cl.proyectoEcommerce.ms_productos.services;

import cl.proyectoEcommerce.ms_productos.dto.ProductoRequestDTO;
import cl.proyectoEcommerce.ms_productos.dto.ProductoResponseDTO;
import cl.proyectoEcommerce.ms_productos.events.ProductoModificadoEvent;
import cl.proyectoEcommerce.ms_productos.exceptions.ProductoNotFoundException;
import cl.proyectoEcommerce.ms_productos.messaging.ProductoAuditPublisher;
import cl.proyectoEcommerce.ms_productos.models.Producto;
import cl.proyectoEcommerce.ms_productos.repositories.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

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

        Producto producto = new Producto();

        producto.setNombre(requestDTO.nombre());
        producto.setDescripcion(requestDTO.descripcion());
        producto.setPrecio(requestDTO.precio());
        producto.setStock(requestDTO.stock());
        producto.setCategoria(requestDTO.categoria());
        producto.setImagenUrl(requestDTO.imagenUrl());

        Producto productoGuardado = productoRepository.save(producto);

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

        // Crear evento de auditoría
        ProductoModificadoEvent evento = new ProductoModificadoEvent(
                productoActualizado.getId(),
                usuarioOid,
                productoActualizado.getNombre(),
                productoActualizado.getPrecio(),
                productoActualizado.getStock(),
                LocalDateTime.now()
        );

        // Publicar evento en RabbitMQ
        productoAuditPublisher.publicarProductoModificado(evento);

        System.out.println(
                "AUDITORÍA PUBLICADA - productoId="
                        + productoActualizado.getId()
                        + ", usuarioOid="
                        + usuarioOid
        );

        return mapToResponseDTO(productoActualizado);
    }

    @Transactional
    public void eliminarProducto(Long id) {

        if (!productoRepository.existsById(id)) {
            throw new ProductoNotFoundException(id);
        }

        productoRepository.deleteById(id);
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

    @Transactional
    public void descontarStock(Long productoId, Integer cantidad) {

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto " + productoId
            );
        }

        int stockAnterior = producto.getStock();

        producto.setStock(stockAnterior - cantidad);

        productoRepository.save(producto);

        System.out.println(
                "STOCK DESCONTADO - productoId=" + productoId +
                ", cantidad=" + cantidad +
                ", stockAnterior=" + stockAnterior +
                ", stockNuevo=" + producto.getStock()
        );
    }
}
