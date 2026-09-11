package com.backend.nutri_predic.conocimiento.evaluacion.repository;

import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.conocimiento.evaluacion.entity.ResultadoTest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoTestRepository extends JpaRepository<ResultadoTest, Long> {
    List<ResultadoTest> findByClienteIdOrderByFechaDesc(Long clienteId);

    Optional<ResultadoTest> findFirstByClienteIdAndFechaLessThanOrderByFechaDescIdDesc(
            Long clienteId, Instant limiteExclusivo);

    long countByNivel(NivelConocimiento nivel);

    List<ResultadoTest> findByEstadoValidezAndNivelIsNotNull(EstadoValidezMedicion estadoValidez);
}
