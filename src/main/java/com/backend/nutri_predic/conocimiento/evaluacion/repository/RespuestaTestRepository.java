package com.backend.nutri_predic.conocimiento.evaluacion.repository;

import com.backend.nutri_predic.conocimiento.evaluacion.entity.RespuestaTest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespuestaTestRepository extends JpaRepository<RespuestaTest, Long> {
    java.util.List<RespuestaTest> findByResultadoIdOrderByIdAsc(Long id);
}
