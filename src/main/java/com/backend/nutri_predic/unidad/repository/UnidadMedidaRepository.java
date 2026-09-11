package com.backend.nutri_predic.unidad.repository;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
    Optional<UnidadMedida> findByCodigoIgnoreCaseAndActivaTrue(String codigo);

    boolean existsByCodigo(String codigo);

    List<UnidadMedida> findByActivaTrueOrderByCodigoAsc();
}
