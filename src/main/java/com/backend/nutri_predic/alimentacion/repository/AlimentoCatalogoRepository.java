package com.backend.nutri_predic.alimentacion.repository;

import com.backend.nutri_predic.alimentacion.entity.AlimentoCatalogo;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlimentoCatalogoRepository extends JpaRepository<AlimentoCatalogo, Long> {
    List<AlimentoCatalogo> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);
}
