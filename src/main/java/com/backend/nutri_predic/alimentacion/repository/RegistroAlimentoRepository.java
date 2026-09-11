package com.backend.nutri_predic.alimentacion.repository;

import com.backend.nutri_predic.alimentacion.entity.RegistroAlimento;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroAlimentoRepository extends JpaRepository<RegistroAlimento, Long> {
    List<RegistroAlimento> findByRegistroHabitoIdOrderByIdAsc(Long id);

    List<RegistroAlimento> findByRegistroHabitoClienteIdOrderByRegistroHabitoFechaDescIdDesc(
            Long clienteId);

    List<RegistroAlimento>
            findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                    Long clienteId, java.time.LocalDate inicio, java.time.LocalDate fin);

    void deleteByRegistroHabitoId(Long id);
}
