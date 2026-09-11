package com.backend.nutri_predic.consumo.repository;

import com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CriterioConsumoSuplementosRepository
        extends JpaRepository<CriterioConsumoSuplementos, Long> {
    List<CriterioConsumoSuplementos> findByRubricaIdOrderByIdAsc(Long rubricaId);

    Optional<CriterioConsumoSuplementos> findByIdAndRubricaId(Long id, Long rubricaId);
}
