package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluacionPerfilHabitosRepository
        extends JpaRepository<EvaluacionPerfilHabitos, Long> {
    List<EvaluacionPerfilHabitos> findAllByOrderByIdAsc();
}
