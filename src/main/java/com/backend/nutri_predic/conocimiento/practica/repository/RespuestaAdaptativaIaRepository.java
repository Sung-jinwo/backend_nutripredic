package com.backend.nutri_predic.conocimiento.practica.repository;

import com.backend.nutri_predic.conocimiento.practica.entity.RespuestaAdaptativaIa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespuestaAdaptativaIaRepository
        extends JpaRepository<RespuestaAdaptativaIa, Long> {
    List<RespuestaAdaptativaIa> findBySesionIdOrderByPreguntaGeneradaIaOrdenAsc(
            Long sesionId);

    boolean existsBySesionId(Long sesionId);
}
