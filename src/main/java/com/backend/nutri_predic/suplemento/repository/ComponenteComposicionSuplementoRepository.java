package com.backend.nutri_predic.suplemento.repository;

import com.backend.nutri_predic.suplemento.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComponenteComposicionSuplementoRepository
        extends JpaRepository<ComponenteComposicionSuplemento, Long> {
    List<ComponenteComposicionSuplemento> findByComposicionIdOrderByIdAsc(Long id);
}
