package com.backend.nutri_predic.conocimiento.service;

import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.conocimiento.dto.*;
import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;
import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento.EstadoPregunta;
import com.backend.nutri_predic.conocimiento.repository.PreguntaConocimientoRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConocimientoService {
    private final PreguntaConocimientoRepository preguntas;

    public ConocimientoService(PreguntaConocimientoRepository p) {
        preguntas = p;
    }

    @Transactional(readOnly = true)
    public List<PreguntaPublicaResponse> listPublic() {
        return preguntas.findByEstadoPreguntaOrderByIdAsc(EstadoPregunta.ACTIVA).stream()
                .map(PreguntaPublicaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PreguntaAdminResponse> listAdmin() {
        return preguntas
                .findAll(
                        org.springframework.data.domain.Sort.by("grupoVersion")
                                .ascending()
                                .and(
                                        org.springframework.data.domain.Sort.by("version")
                                                .ascending()))
                .stream()
                .map(PreguntaAdminResponse::from)
                .toList();
    }

    @Transactional
    public PreguntaAdminResponse create(PreguntaRequest r) {
        return PreguntaAdminResponse.from(preguntas.save(copy(new PreguntaConocimiento(), r)));
    }

    @Transactional
    public PreguntaAdminResponse createVersion(Long id, PreguntaRequest r) {
        var old =
                preguntas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pregunta"));
        old.setEstadoPregunta(EstadoPregunta.INACTIVA);
        preguntas.save(old);
        var n = copy(new PreguntaConocimiento(), r);
        n.setGrupoVersion(old.getGrupoVersion());
        n.setVersion(old.getVersion() + 1);
        n.setVigenteDesde(Instant.now());
        return PreguntaAdminResponse.from(preguntas.save(n));
    }

    @Transactional
    public void deactivate(Long id) {
        var p = preguntas.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pregunta"));
        p.setEstadoPregunta(EstadoPregunta.INACTIVA);
        preguntas.save(p);
    }

    private PreguntaConocimiento copy(PreguntaConocimiento p, PreguntaRequest r) {
        p.setEnunciado(r.texto().trim());
        p.setTema(r.tema().trim());
        p.setCategoria(r.tema().trim());
        p.setSubtema(trim(r.subtema()));
        p.setDificultad(r.dificultad().trim());
        p.setOpcionA(r.opcionA().trim());
        p.setOpcionB(r.opcionB().trim());
        p.setOpcionC(r.opcionC().trim());
        p.setOpcionD(r.opcionD().trim());
        p.setRespuestaCorrecta(r.respuestaCorrecta().trim().toUpperCase());
        p.setExplicacion(trim(r.explicacion()));
        p.setFuenteReferencia(trim(r.fuenteReferencia()));
        return p;
    }

    private String trim(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
