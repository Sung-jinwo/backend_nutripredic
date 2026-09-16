package com.backend.nutri_predic.orientacion.repository;

import com.backend.nutri_predic.orientacion.entity.AdaptacionDiaria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdaptacionDiariaRepository extends JpaRepository<AdaptacionDiaria, Long> {
    Optional<AdaptacionDiaria> findByPrediccionModeloId(Long prediccionId);
    Optional<AdaptacionDiaria> findFirstByClienteIdOrderByFechaAplicacionDescIdDesc(Long clienteId);
}
