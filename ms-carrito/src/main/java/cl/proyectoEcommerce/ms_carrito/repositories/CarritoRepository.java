package cl.proyectoEcommerce.ms_carrito.repositories;

import cl.proyectoEcommerce.ms_carrito.models.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    // Buscar carrito por el OID del usuario en Azure AD
    Optional<Carrito> findByUsuarioOid(String usuarioOid);
}
