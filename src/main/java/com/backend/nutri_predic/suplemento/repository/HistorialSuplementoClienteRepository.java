package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.HistorialSuplementoCliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialSuplementoClienteRepository
        extends JpaRepository<HistorialSuplementoCliente, Long> {
    List<HistorialSuplementoCliente> findByAsignacionIdOrderByRegistradoEnAscIdAsc(Long id);
}
