package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.*;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EquivalenciaUnidadSuplementoRepository
        extends JpaRepository<EquivalenciaUnidadSuplemento, Long> {
    List<EquivalenciaUnidadSuplemento> findBySuplementoIdOrderByVersionDesc(Long id);

    Optional<EquivalenciaUnidadSuplemento> findTopBySuplementoIdOrderByVersionDesc(Long id);

    @Query(
            "select e from EquivalenciaUnidadSuplemento e where e.suplemento.id=:s and e.activo=true and lower(e.unidadOrigen.codigo)=lower(:o) and lower(e.unidadDestino.codigo)=lower(:d) and e.fechaDesde<=:f and (e.fechaHasta is null or e.fechaHasta>=:f) order by e.version desc")
    List<EquivalenciaUnidadSuplemento> activas(
            @Param("s") Long suplemento,
            @Param("o") String origen,
            @Param("d") String destino,
            @Param("f") LocalDate fecha);
}
