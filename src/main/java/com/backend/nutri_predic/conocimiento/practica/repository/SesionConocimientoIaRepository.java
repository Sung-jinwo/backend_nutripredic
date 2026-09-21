package com.backend.nutri_predic.conocimiento.practica.repository;

import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SesionConocimientoIaRepository extends JpaRepository<SesionConocimientoIa, Long> {
    Optional<SesionConocimientoIa> findFirstByClienteIdAndFechaEvaluacionAndPrediccionModeloIsNotNullOrderByCreadoEnDesc(Long clienteId, java.time.LocalDate fechaEvaluacion);
    Optional<SesionConocimientoIa> findByClienteIdAndConfiguracionVersion(Long clienteId, String configuracionVersion);
    Optional<SesionConocimientoIa> findFirstByClienteIdOrderByCreadoEnDesc(Long clienteId);
    Optional<SesionConocimientoIa> findByPrediccionModeloIdAndConfiguracionVersion(
            Long prediccionId, String configuracionVersion);

    Optional<SesionConocimientoIa> findFirstByPrediccionModeloClienteIdOrderByCreadoEnDesc(
            Long clienteId);

    Optional<SesionConocimientoIa> findFirstByClienteIdAndFechaEvaluacionOrderByCreadoEnDesc(
                        Long clienteId, java.time.LocalDate fechaEvaluacion);

    Optional<SesionConocimientoIa> findFirstByClienteIdAndEstadoAndFechaEvaluacionBeforeOrderByFechaEvaluacionDescCreadoEnDesc(
            Long clienteId,
            com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa estado,
            java.time.LocalDate fechaEvaluacion);

    List<SesionConocimientoIa> findByEstadoAndEstadoValidez(
            com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa estado,
            com.backend.nutri_predic.common.enums.EstadoValidezMedicion estadoValidez);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionConocimientoIa s where s.id = :id")
    Optional<SesionConocimientoIa> findByIdForUpdate(@Param("id") Long id);
}
