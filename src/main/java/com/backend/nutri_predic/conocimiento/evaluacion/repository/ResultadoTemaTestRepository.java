package com.backend.nutri_predic.conocimiento.evaluacion.repository;

import com.backend.nutri_predic.conocimiento.evaluacion.entity.ResultadoTemaTest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoTemaTestRepository extends JpaRepository<ResultadoTemaTest, Long> {
    List<ResultadoTemaTest> findByResultadoIdOrderByTemaAsc(Long id);
}
