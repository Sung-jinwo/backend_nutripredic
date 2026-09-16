package com.backend.nutri_predic.prediccionmodelo.evento.repository;

import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;

public interface EventoAnalisisRepository extends JpaRepository<EventoAnalisis, Long> {
    Optional<EventoAnalisis> findFirstByPrediccionModeloIdOrderByResultadoDisponibleEnDescIdDesc(Long prediccionId);
    Optional<EventoAnalisis> findFirstByPrediccionModeloIdAndOrigenResultadoOrderByIdAsc(
            Long prediccionId, OrigenResultadoAnalisis origenResultado);
}
