package com.backend.nutri_predic.consumo.repository;

import com.backend.nutri_predic.consumo.entity.SnapshotEvaluacionConsumo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotEvaluacionConsumoRepository
        extends JpaRepository<SnapshotEvaluacionConsumo, Long> {
    Optional<SnapshotEvaluacionConsumo> findByEvaluacionId(Long evaluacionId);
}
