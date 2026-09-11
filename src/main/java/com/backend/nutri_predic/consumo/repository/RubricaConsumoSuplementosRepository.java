package com.backend.nutri_predic.consumo.repository;

import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubricaConsumoSuplementosRepository
        extends JpaRepository<RubricaConsumoSuplementos, Long> {
    List<RubricaConsumoSuplementos> findAllByOrderByVersionDescIdDesc();

    boolean existsByCodigoAndVersion(String codigo, Integer version);

    boolean existsByCodigoAndVersionAndIdNot(String codigo, Integer version, Long id);
}
