package com.backend.nutri_predic.estudio.repository;

import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipacionEstudioRepository extends JpaRepository<ParticipacionEstudio, Long> {
    boolean existsByEstudioIdAndClienteId(Long estudioId, Long clienteId);

    List<ParticipacionEstudio> findByEstudioIdOrderByCodigoParticipanteAsc(Long estudioId);
}
