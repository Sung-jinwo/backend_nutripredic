package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.ResultadoDimensionPerfilHabitos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoDimensionPerfilHabitosRepository
        extends JpaRepository<ResultadoDimensionPerfilHabitos, Long> {
    List<ResultadoDimensionPerfilHabitos> findByEvaluacionIdOrderByDimensionOrdenAsc(Long evaluacionId);
}
