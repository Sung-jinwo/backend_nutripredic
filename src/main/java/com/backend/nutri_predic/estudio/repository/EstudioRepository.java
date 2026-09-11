package com.backend.nutri_predic.estudio.repository;

import com.backend.nutri_predic.estudio.entity.Estudio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudioRepository extends JpaRepository<Estudio, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
}
