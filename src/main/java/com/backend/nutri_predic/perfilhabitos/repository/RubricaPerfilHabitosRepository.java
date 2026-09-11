package com.backend.nutri_predic.perfilhabitos.repository;

import com.backend.nutri_predic.perfilhabitos.entity.RubricaPerfilHabitos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubricaPerfilHabitosRepository extends JpaRepository<RubricaPerfilHabitos, Long> {
    List<RubricaPerfilHabitos> findAllByOrderByCodigoAscVersionDesc();
}
