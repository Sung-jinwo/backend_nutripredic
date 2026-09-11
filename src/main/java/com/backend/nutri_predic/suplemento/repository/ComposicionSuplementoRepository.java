package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.*;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ComposicionSuplementoRepository
        extends JpaRepository<ComposicionSuplemento, Long> {
    @Query(
            "select c from ComposicionSuplemento c where c.suplemento.id=:s and c.activo=true and c.fechaDesde<=:f and (c.fechaHasta is null or c.fechaHasta>=:f) order by c.version desc")
    List<ComposicionSuplemento> activas(@Param("s") Long suplemento, @Param("f") LocalDate fecha);

    Optional<ComposicionSuplemento> findTopBySuplementoIdOrderByVersionDesc(Long id);

    List<ComposicionSuplemento> findBySuplementoIdOrderByVersionDesc(Long id);
}
