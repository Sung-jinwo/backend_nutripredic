package com.backend.nutri_predic.alimentacion.repository;

import com.backend.nutri_predic.alimentacion.entity.ComposicionNutricionalAlimento;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComposicionNutricionalAlimentoRepository
        extends JpaRepository<ComposicionNutricionalAlimento, Long> {
    List<ComposicionNutricionalAlimento> findByAlimentoIdOrderByVersionDesc(Long alimentoId);

    @Query(
            "select c from ComposicionNutricionalAlimento c where c.alimento.id = :alimentoId and c.activo = true and c.fechaDesde <= :fecha and (c.fechaHasta is null or c.fechaHasta >= :fecha) order by c.version desc")
    List<ComposicionNutricionalAlimento> findActivasAplicables(
            @Param("alimentoId") Long alimentoId, @Param("fecha") LocalDate fecha);

    Optional<ComposicionNutricionalAlimento> findTopByAlimentoIdOrderByVersionDesc(Long alimentoId);
}
