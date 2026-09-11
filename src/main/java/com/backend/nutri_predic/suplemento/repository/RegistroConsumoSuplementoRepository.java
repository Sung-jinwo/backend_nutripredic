package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.RegistroConsumoSuplemento;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistroConsumoSuplementoRepository
        extends JpaRepository<RegistroConsumoSuplemento, Long> {
    List<RegistroConsumoSuplemento> findByRegistroHabitoIdOrderByIdAsc(Long id);

    List<RegistroConsumoSuplemento>
            findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                    Long clienteId, LocalDate inicio, LocalDate fin);

    boolean existsByRegistroHabitoId(Long id);

    boolean existsByRegistroHabitoIdAndSuplementoClienteId(Long habitoId, Long suplementoClienteId);

    void deleteByRegistroHabitoId(Long id);
}
