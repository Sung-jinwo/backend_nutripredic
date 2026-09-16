package com.backend.nutri_predic.consumo.repository;

import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RubricaConsumoSuplementosRepository
        extends JpaRepository<RubricaConsumoSuplementos, Long> {
    List<RubricaConsumoSuplementos> findAllByOrderByVersionDescIdDesc();

    @org.springframework.data.jpa.repository.Query("select r from RubricaConsumoSuplementos r where exists (select c.id from CriterioConsumoSuplementos c where c.rubrica = r) order by r.version desc, r.id desc")
    List<RubricaConsumoSuplementos> findConCriteriosOrderByVersionDescIdDesc();

    boolean existsByCodigoAndVersion(String codigo, Integer version);

    boolean existsByCodigoAndVersionAndIdNot(String codigo, Integer version, Long id);
}
