package com.backend.nutri_predic.prediccionmodelo.evento.controller;

import com.backend.nutri_predic.prediccionmodelo.evento.dto.ProcedimientoAnalisisResponse;
import com.backend.nutri_predic.prediccionmodelo.evento.service.ProcedimientoAnalisisService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procedimientos-analisis")
public class ProcedimientoAnalisisController {
    private final ProcedimientoAnalisisService service;

    public ProcedimientoAnalisisController(ProcedimientoAnalisisService s) {
        service = s;
    }

    @GetMapping
    public List<ProcedimientoAnalisisResponse> activos() {
        return service.activos();
    }

    @GetMapping("/{codigo}")
    public ProcedimientoAnalisisResponse porCodigo(@PathVariable String codigo) {
        return service.porCodigo(codigo);
    }
}
