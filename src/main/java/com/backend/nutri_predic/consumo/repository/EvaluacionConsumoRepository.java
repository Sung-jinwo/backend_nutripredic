package com.backend.nutri_predic.consumo.repository;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.consumo.entity.EstadoClasificacionConsumo;
import com.backend.nutri_predic.consumo.entity.EvaluacionConsumo;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionConsumoRepository extends JpaRepository<EvaluacionConsumo, Long> {
    List<EvaluacionConsumo> findByClienteIdOrderByFechaEvaluacionDesc(Long clienteId);
    Optional<EvaluacionConsumo> findFirstByClienteIdOrderByFechaCorteDesc(Long clienteId);
    Optional<EvaluacionConsumo> findFirstByPrediccionModeloIdOrderByFechaEvaluacionDesc(Long prediccionModeloId);

    Optional<EvaluacionConsumo>
            findFirstByClienteIdAndFechaCorteAndFechaInicioAndVentanaDiasAndSchemaVersionOrderByFechaEvaluacionDesc(
                    Long clienteId, LocalDate fechaCorte, LocalDate fechaInicio, Integer ventanaDias, String schemaVersion);

    @Query(
            """
            select evaluacion from EvaluacionConsumo evaluacion
            where evaluacion.estadoValidez = :estadoValidez
              and evaluacion.estadoClasificacion in :clasificaciones
              and evaluacion.altoConsumo is not null
              and evaluacion.rubrica is not null
              and evaluacion.rubrica.validada = true
            order by evaluacion.cliente.id, evaluacion.fechaCorte, evaluacion.id
            """)
    List<EvaluacionConsumo> findCandidatasPcsOficial(
            @Param("estadoValidez") EstadoValidezMedicion estadoValidez,
            @Param("clasificaciones") List<EstadoClasificacionConsumo> clasificaciones);
}
