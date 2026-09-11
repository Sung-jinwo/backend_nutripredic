package com.backend.nutri_predic.conocimiento.service;

import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.conocimiento.dto.InstrumentoPublicoResponse;
import com.backend.nutri_predic.conocimiento.entity.EstadoInstrumento;
import com.backend.nutri_predic.conocimiento.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstrumentoConocimientoService {
    private final InstrumentoConocimientoRepository instrumentos;
    private final InstrumentoPreguntaRepository preguntas;

    public InstrumentoConocimientoService(
            InstrumentoConocimientoRepository i, InstrumentoPreguntaRepository p) {
        instrumentos = i;
        preguntas = p;
    }

    @Transactional(readOnly = true)
    public InstrumentoPublicoResponse activo() {
        var i =
                instrumentos
                        .findFirstByEstadoOrderByVigenteDesdeDescIdDesc(EstadoInstrumento.ACTIVO)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "No existe un instrumento activo"));
        return InstrumentoPublicoResponse.from(
                i, preguntas.findByInstrumentoIdOrderByOrdenAsc(i.getId()));
    }
}
