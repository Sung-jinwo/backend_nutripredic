package com.backend.nutri_predic.alimentacion.repository;

import com.backend.nutri_predic.alimentacion.entity.EquivalenciaUnidadAlimento;
import java.time.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface EquivalenciaUnidadAlimentoRepository
        extends JpaRepository<EquivalenciaUnidadAlimento, Long> {
    List<EquivalenciaUnidadAlimento> findByAlimentoIdOrderByVersionDesc(Long id);

    Optional<EquivalenciaUnidadAlimento> findTopByAlimentoIdOrderByVersionDesc(Long id);

    @Query(
            "select e from EquivalenciaUnidadAlimento e where e.alimento.id=:a and e.activo=true and e.fechaDesde<=:f and (e.fechaHasta is null or e.fechaHasta>=:f) order by e.version desc")
    List<EquivalenciaUnidadAlimento> activasFecha(
            @Param("a") Long alimento, @Param("f") LocalDate fecha);

    @Query(
            "select e from EquivalenciaUnidadAlimento e where e.alimento.id=:a and e.activo=true and lower(e.unidadOrigen.codigo)=lower(:o) and lower(e.unidadDestino.codigo)=lower(:d) and e.fechaDesde<=:f and (e.fechaHasta is null or e.fechaHasta>=:f) order by e.version desc")
    List<EquivalenciaUnidadAlimento> activas(
            @Param("a") Long alimento,
            @Param("o") String origen,
            @Param("d") String destino,
            @Param("f") LocalDate fecha);
}
