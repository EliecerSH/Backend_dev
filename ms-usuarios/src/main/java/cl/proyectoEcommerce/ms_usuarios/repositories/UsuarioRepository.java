package cl.proyectoEcommerce.ms_usuarios.repositories;

import cl.proyectoEcommerce.ms_usuarios.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByAzureOid(String azureOid);

    boolean existsByAzureOid(String azureOid);
}
