package cl.proyectoEcommerce.ms_auditoria.repositories;

import cl.proyectoEcommerce.ms_auditoria.models.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
}
