package com.backend.nutri_predic.prediccionmodelo.evento.service;

import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.prediccionmodelo.evento.dto.ProcedimientoAnalisisResponse;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.ProcedimientoAnalisisRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcedimientoAnalisisService {
    private final ProcedimientoAnalisisRepository repo;

    public ProcedimientoAnalisisService(ProcedimientoAnalisisRepository r) {
        repo = r;
    }

    @Transactional(readOnly = true)
    public List<ProcedimientoAnalisisResponse> activos() {
        return repo.findByActivoTrueOrderByCodigoAsc().stream()
                .map(ProcedimientoAnalisisResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimientoAnalisisResponse porCodigo(String codigo) {
        return ProcedimientoAnalisisResponse.from(
                repo.findFirstByCodigoAndActivoTrueOrderByVersionDesc(codigo)
                        .orElseThrow(() -> new ResourceNotFoundException("Procedimiento")));
    }
}
