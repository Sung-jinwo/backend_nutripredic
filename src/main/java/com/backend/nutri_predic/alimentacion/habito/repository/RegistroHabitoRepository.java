package com.backend.nutri_predic.alimentacion.habito.repository;

import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {
    List<RegistroHabito> findByClienteIdOrderByFechaDesc(Long id);

    Optional<RegistroHabito> findByClienteIdAndFecha(Long id, LocalDate fecha);

    boolean existsByClienteIdAndFecha(Long id, LocalDate fecha);

    List<RegistroHabito> findByClienteIdAndFechaLessThanEqualOrderByFechaAscIdAsc(
            Long clienteId, LocalDate fechaCorte);

    List<RegistroHabito> findByClienteIdAndFechaBetweenOrderByFechaAscIdAsc(
            Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
