package com.backend.nutri_predic.conocimiento.repository;

import com.backend.nutri_predic.conocimiento.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentoConocimientoRepository
        extends JpaRepository<InstrumentoConocimiento, Long> {
    Optional<InstrumentoConocimiento> findFirstByEstadoOrderByVigenteDesdeDescIdDesc(
            EstadoInstrumento estado);
}
