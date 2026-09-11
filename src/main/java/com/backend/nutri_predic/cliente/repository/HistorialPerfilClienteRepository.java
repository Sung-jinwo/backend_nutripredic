package com.backend.nutri_predic.cliente.repository;

import com.backend.nutri_predic.cliente.entity.HistorialPerfilCliente;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialPerfilClienteRepository
        extends JpaRepository<HistorialPerfilCliente, Long> {
    List<HistorialPerfilCliente> findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(Long id);

    List<HistorialPerfilCliente>
            findByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(
                    Long id, LocalDate fecha);

    Optional<HistorialPerfilCliente>
            findFirstByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(
                    Long id, LocalDate fecha);
}
