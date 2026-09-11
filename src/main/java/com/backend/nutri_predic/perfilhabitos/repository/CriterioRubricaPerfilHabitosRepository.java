package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.CriterioRubricaPerfilHabitos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterioRubricaPerfilHabitosRepository
        extends JpaRepository<CriterioRubricaPerfilHabitos, Long> {
    List<CriterioRubricaPerfilHabitos> findByRubricaIdOrderByDimensionOrdenAscOrdenAsc(Long rubricaId);
}
