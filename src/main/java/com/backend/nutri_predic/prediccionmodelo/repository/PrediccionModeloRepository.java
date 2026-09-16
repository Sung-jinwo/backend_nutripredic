package com.backend.nutri_predic.prediccionmodelo.repository;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrediccionModeloRepository extends JpaRepository<PrediccionModelo, Long> {
    Optional<PrediccionModelo> findFirstByOrderByFechaPrediccionDesc();

    Optional<PrediccionModelo> findFirstByClienteIdOrderByFechaPrediccionDesc(Long clienteId);

    List<PrediccionModelo> findByClienteIdOrderByFechaPrediccionDesc(Long clienteId);

    List<PrediccionModelo> findByClienteIdAndEstadoOrderByFechaPrediccionDesc(
            Long clienteId, EstadoPrediccionModelo estado);

    Optional<PrediccionModelo>
            findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndModelVersionAndEstadoOrderByFechaPrediccionDesc(
                    Long clienteId,
                    LocalDate fechaCorte,
                    MomentoEvaluacion momentoEvaluacion,
                    String modelVersion,
                    EstadoPrediccionModelo estado);

    Optional<PrediccionModelo>
            findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndParticipacionEstudioIdAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                    Long clienteId,
                    LocalDate fechaCorte,
                    MomentoEvaluacion momentoEvaluacion,
                    Long participacionEstudioId,
                    String schemaVersion,
                    EstadoPrediccionModelo estado);

    Optional<PrediccionModelo>
            findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                    Long clienteId,
                    LocalDate fechaCorte,
                    MomentoEvaluacion momentoEvaluacion,
                    String schemaVersion,
                    EstadoPrediccionModelo estado);

    Optional<PrediccionModelo>
            findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndParticipacionEstudioIdAndSchemaVersionAndModelVersionAndEstadoOrderByFechaPrediccionDesc(
                    Long clienteId,
                    LocalDate fechaCorte,
                    MomentoEvaluacion momentoEvaluacion,
                    Long participacionEstudioId,
                    String schemaVersion,
                    String modelVersion,
                    EstadoPrediccionModelo estado);
}
