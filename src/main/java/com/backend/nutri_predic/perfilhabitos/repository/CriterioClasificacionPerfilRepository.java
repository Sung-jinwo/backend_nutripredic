package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.CriterioClasificacionPerfil;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterioClasificacionPerfilRepository
        extends JpaRepository<CriterioClasificacionPerfil, Long> {
    List<CriterioClasificacionPerfil> findByRubricaIdOrderByOrdenAsc(Long rubricaId);
}
