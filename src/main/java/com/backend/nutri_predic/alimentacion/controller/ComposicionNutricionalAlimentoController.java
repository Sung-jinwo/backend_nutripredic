package com.backend.nutri_predic.alimentacion.controller;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.service.ComposicionNutricionalAlimentoService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/alimentos/{alimentoId}/composiciones")
@PreAuthorize("hasRole('ADMIN')")
public class ComposicionNutricionalAlimentoController {
    private final ComposicionNutricionalAlimentoService service;

    public ComposicionNutricionalAlimentoController(ComposicionNutricionalAlimentoService s) {
        service = s;
    }

    @GetMapping
    public List<ComposicionNutricionalAlimentoResponse> listar(@PathVariable Long alimentoId) {
        return service.listar(alimentoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComposicionNutricionalAlimentoResponse crear(
            @PathVariable Long alimentoId,
            @Valid @RequestBody ComposicionNutricionalAlimentoRequest r) {
        return service.crear(alimentoId, r);
    }

    @GetMapping("/activa")
    public ComposicionNutricionalAlimentoResponse activa(
            @PathVariable Long alimentoId, @RequestParam(required = false) LocalDate fecha) {
        return service.activa(alimentoId, fecha == null ? LocalDate.now() : fecha);
    }
}
