package com.backend.nutri_predic.conocimiento.repository;

import com.backend.nutri_predic.conocimiento.entity.InstrumentoPregunta;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentoPreguntaRepository extends JpaRepository<InstrumentoPregunta, Long> {
    List<InstrumentoPregunta> findByInstrumentoIdOrderByOrdenAsc(Long instrumentoId);

    boolean existsByInstrumentoIdAndPreguntaId(Long instrumentoId, Long preguntaId);
}
