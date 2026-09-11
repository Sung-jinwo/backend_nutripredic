package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.SuplementoCliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuplementoClienteRepository extends JpaRepository<SuplementoCliente, Long> {
    List<SuplementoCliente> findByClienteId(Long id);

    List<SuplementoCliente> findByClienteIdOrderBySuplementoNombreAscIdAsc(Long clienteId);

    Optional<SuplementoCliente> findByClienteIdAndSuplementoId(Long clienteId, Long suplementoId);

    List<SuplementoCliente> findByClienteIdAndActivoTrueOrderBySuplementoNombreAscIdAsc(
            Long clienteId);
}
