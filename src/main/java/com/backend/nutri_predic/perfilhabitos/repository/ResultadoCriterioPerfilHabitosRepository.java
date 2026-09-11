package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.ResultadoCriterioPerfilHabitos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoCriterioPerfilHabitosRepository
        extends JpaRepository<ResultadoCriterioPerfilHabitos, Long> {
    List<ResultadoCriterioPerfilHabitos> findByEvaluacionIdOrderByCriterioDimensionOrdenAscCriterioOrdenAsc(
            Long evaluacionId);
}
