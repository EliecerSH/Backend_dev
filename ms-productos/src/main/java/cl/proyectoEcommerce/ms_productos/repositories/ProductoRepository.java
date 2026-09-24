package cl.proyectoEcommerce.ms_productos.repositories;

import cl.proyectoEcommerce.ms_productos.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByCategoriaIgnoreCase(String categoria);

    // Buscar productos cuyo nombre contenga el texto buscado (ignorando mayúsculas/minúsculas)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
