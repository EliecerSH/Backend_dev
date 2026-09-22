package cl.ProyectoEcommerce.ms_ordenes.repositories;

import cl.ProyectoEcommerce.ms_ordenes.models.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {

    // Historial de compras de un usuario, más recientes primero
    List<Orden> findByUsuarioOidOrderByCreadoEnDesc(String usuarioOid);
}
