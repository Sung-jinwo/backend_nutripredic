package com.backend.nutri_predic.conocimiento.practica.repository;

import com.backend.nutri_predic.conocimiento.practica.entity.PreguntaGeneradaIa;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaGeneradaIaRepository extends JpaRepository<PreguntaGeneradaIa, Long> {
    List<PreguntaGeneradaIa> findBySesionIdOrderByOrdenAsc(Long sesionId);

    List<PreguntaGeneradaIa> findBySesionIdAndIdIn(Long sesionId, Collection<Long> ids);

    long countBySesionId(Long sesionId);

    void deleteBySesionId(Long sesionId);
}
