package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuplementoCatalogoRepository extends JpaRepository<SuplementoCatalogo, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    Optional<SuplementoCatalogo> findByNombreIgnoreCase(String nombre);
}
