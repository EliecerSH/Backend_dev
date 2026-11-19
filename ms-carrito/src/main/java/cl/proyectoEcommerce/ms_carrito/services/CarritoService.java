package cl.proyectoEcommerce.ms_carrito.services;

import cl.proyectoEcommerce.ms_carrito.clients.ProductoClient;
import cl.proyectoEcommerce.ms_carrito.dto.AgregarItemRequestDTO;
import cl.proyectoEcommerce.ms_carrito.dto.CarritoItemResponseDTO;
import cl.proyectoEcommerce.ms_carrito.dto.CarritoResponseDTO;
import cl.proyectoEcommerce.ms_carrito.dto.ProductoClientDTO;
import cl.proyectoEcommerce.ms_carrito.exceptions.ItemNoEncontradoException;
import cl.proyectoEcommerce.ms_carrito.exceptions.StockInsuficienteException;
import cl.proyectoEcommerce.ms_carrito.models.Carrito;
import cl.proyectoEcommerce.ms_carrito.models.CarritoItem;
import cl.proyectoEcommerce.ms_carrito.repositories.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoClient productoClient;

    // Ya NO se inyecta RestClient.Builder aquí -- eso vive dentro de ProductoClient
    public CarritoService(CarritoRepository carritoRepository, ProductoClient productoClient) {
        this.carritoRepository = carritoRepository;
        this.productoClient = productoClient;
    }

    @Transactional
    public CarritoResponseDTO obtenerOcrearCarrito(String usuarioOid) {
        Carrito carrito = carritoRepository.findByUsuarioOid(usuarioOid)
                .orElseGet(() -> carritoRepository.save(new Carrito(usuarioOid)));

        return mapToResponseDTO(carrito);
    }

    @Transactional
    public CarritoResponseDTO agregarProducto(String usuarioOid, AgregarItemRequestDTO request) {
        // 1. Obtener info del producto vía el cliente dedicado (con circuit breaker)
        ProductoClientDTO producto = productoClient.consultarProducto(request.productoId());

        // 2. Validar stock suficiente
        if (producto.stock() < request.cantidad()) {
            throw new StockInsuficienteException("Stock insuficiente. Disponible: " + producto.stock());
        }

        // 3. Buscar o crear el carrito del usuario
        Carrito carrito = carritoRepository.findByUsuarioOid(usuarioOid)
                .orElseGet(() -> carritoRepository.save(new Carrito(usuarioOid)));

        // 4. Verificar si el producto ya está en el carrito
        Optional<CarritoItem> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(request.productoId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            CarritoItem item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + request.cantidad();
            if (producto.stock() < nuevaCantidad) {
                throw new StockInsuficienteException("Stock insuficiente para sumar " + request.cantidad()
                        + " unidades más (disponible: " + producto.stock() + ", en tu carrito: " + item.getCantidad() + ").");
            }
            item.setCantidad(nuevaCantidad);
            item.setPrecioUnitario(producto.precio());
        } else {
            CarritoItem nuevoItem = new CarritoItem(request.productoId(), request.cantidad(), producto.precio());
            carrito.agregarItem(nuevoItem);
        }

        Carrito carritoGuardado = carritoRepository.save(carrito);
        return mapToResponseDTO(carritoGuardado);
    }

    @Transactional
    public CarritoResponseDTO eliminarProducto(String usuarioOid, Long productoId) {
        Carrito carrito = carritoRepository.findByUsuarioOid(usuarioOid)
                .orElseThrow(() -> new ItemNoEncontradoException("Carrito no encontrado para el usuario."));

        Optional<CarritoItem> itemAEliminar = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (itemAEliminar.isPresent()) {
            carrito.removerItem(itemAEliminar.get());
            carritoRepository.save(carrito);
        } else {
            throw new ItemNoEncontradoException("El producto con ID " + productoId + " no está en el carrito.");
        }

        return mapToResponseDTO(carrito);
    }

    // Idempotente: vaciar un carrito inexistente o ya vacío no es un error (el frontend lo llama
    // después de un pago aprobado, y antes respondía 500 si el carrito no existía).
    @Transactional
    public void vaciarCarrito(String usuarioOid) {
        carritoRepository.findByUsuarioOid(usuarioOid).ifPresent(carrito -> {
            carrito.getItems().clear();
            carritoRepository.save(carrito);
        });
    }

    // OJO: el método privado consultarProducto() y el campo restClient
    // fueron ELIMINADOS de esta clase -- ahora viven en ProductoClient

    private CarritoResponseDTO mapToResponseDTO(Carrito carrito) {
        List<CarritoItemResponseDTO> itemsDto = carrito.getItems().stream()
                .map(item -> {
                    BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
                    return new CarritoItemResponseDTO(
                            item.getId(),
                            item.getProductoId(),
                            item.getCantidad(),
                            item.getPrecioUnitario(),
                            subtotal
                    );
                })
                .toList();

        BigDecimal total = itemsDto.stream()
                .map(CarritoItemResponseDTO::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarritoResponseDTO(
                carrito.getId(),
                carrito.getUsuarioOid(),
                itemsDto,
                total
        );
    }
}