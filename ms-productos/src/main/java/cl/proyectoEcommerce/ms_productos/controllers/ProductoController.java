package cl.proyectoEcommerce.ms_productos.controllers;

import cl.proyectoEcommerce.ms_productos.dto.ProductoRequestDTO;
import cl.proyectoEcommerce.ms_productos.dto.ProductoResponseDTO;
import cl.proyectoEcommerce.ms_productos.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    // Inyección de dependencias por constructor
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // --- ENDPOINTS PÚBLICOS (LECTURA) ---

    // GET /api/v1/productos -> Listar todos los productos
    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerTodos() {
        List<ProductoResponseDTO> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    // GET /api/v1/productos/{id} -> Obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerPorId(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.obtenerPorId(id);
        return ResponseEntity.ok(producto);
    }

    // GET /api/v1/productos/categoria/{categoria} -> Buscar productos por categoría
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerPorCategoria(@PathVariable String categoria) {
        List<ProductoResponseDTO> productos = productoService.obtenerPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    // --- ENDPOINTS PROTEGIDOS (REQUIEREN JWT BEARER TOKEN DE AZURE) ---

    // POST /api/v1/productos -> Crear un nuevo producto
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO nuevoProducto = productoService.crearProducto(requestDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    // PUT /api/v1/productos/{id} -> Actualizar un producto existente
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO requestDTO) {
        ProductoResponseDTO productoActualizado = productoService.actualizarProducto(id, requestDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    // DELETE /api/v1/productos/{id} -> Eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
