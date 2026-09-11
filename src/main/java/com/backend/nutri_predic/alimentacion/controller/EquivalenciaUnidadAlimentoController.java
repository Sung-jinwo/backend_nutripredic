package com.backend.nutri_predic.alimentacion.controller;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.service.EquivalenciaUnidadAlimentoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/alimentos/{alimentoId}/equivalencias")
@PreAuthorize("hasRole('ADMIN')")
public class EquivalenciaUnidadAlimentoController {
    private final EquivalenciaUnidadAlimentoService s;

    public EquivalenciaUnidadAlimentoController(EquivalenciaUnidadAlimentoService s) {
        this.s = s;
    }

    @GetMapping
    public List<EquivalenciaUnidadAlimentoResponse> listar(@PathVariable Long alimentoId) {
        return s.listar(alimentoId);
    }

    @GetMapping("/activas")
    public List<EquivalenciaUnidadAlimentoResponse> activas(
            @PathVariable Long alimentoId, @RequestParam LocalDate fecha) {
        return s.listarActivas(alimentoId, fecha);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquivalenciaUnidadAlimentoResponse crear(
            @PathVariable Long alimentoId,
            @Valid @RequestBody EquivalenciaUnidadAlimentoRequest r) {
        return s.crear(alimentoId, r);
    }
}
