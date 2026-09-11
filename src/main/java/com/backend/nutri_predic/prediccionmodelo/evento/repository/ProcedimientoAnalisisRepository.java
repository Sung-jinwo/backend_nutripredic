package com.backend.nutri_predic.prediccionmodelo.evento.repository;

import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcedimientoAnalisisRepository
        extends JpaRepository<ProcedimientoAnalisis, Long> {
    List<ProcedimientoAnalisis> findByActivoTrueOrderByCodigoAsc();

    Optional<ProcedimientoAnalisis> findFirstByCodigoAndActivoTrueOrderByVersionDesc(String codigo);
}
