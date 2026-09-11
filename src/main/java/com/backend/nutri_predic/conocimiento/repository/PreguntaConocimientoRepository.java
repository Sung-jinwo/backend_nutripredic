package com.backend.nutri_predic.conocimiento.repository;

import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaConocimientoRepository extends JpaRepository<PreguntaConocimiento, Long> {
    List<PreguntaConocimiento> findByEstadoPreguntaOrderByIdAsc(
            PreguntaConocimiento.EstadoPregunta estado);
}
