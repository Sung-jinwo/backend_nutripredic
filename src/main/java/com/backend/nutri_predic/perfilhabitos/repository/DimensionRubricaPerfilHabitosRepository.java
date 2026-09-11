package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.DimensionRubricaPerfilHabitos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DimensionRubricaPerfilHabitosRepository
        extends JpaRepository<DimensionRubricaPerfilHabitos, Long> {
    List<DimensionRubricaPerfilHabitos> findByRubricaIdOrderByOrdenAsc(Long rubricaId);
}
