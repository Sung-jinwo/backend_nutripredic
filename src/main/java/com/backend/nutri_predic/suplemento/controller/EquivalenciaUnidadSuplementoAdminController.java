package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.service.EquivalenciaUnidadSuplementoService;
import jakarta.validation.Valid;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/suplementos/{suplementoId}/equivalencias")
@PreAuthorize("hasRole('ADMIN')")
public class EquivalenciaUnidadSuplementoAdminController {
    private final EquivalenciaUnidadSuplementoService service;

    public EquivalenciaUnidadSuplementoAdminController(EquivalenciaUnidadSuplementoService s) {
        service = s;
    }

    @GetMapping
    public List<EquivalenciaUnidadSuplementoResponse> listar(@PathVariable Long suplementoId) {
        return service.listarPorSuplemento(suplementoId).stream()
                .map(EquivalenciaUnidadSuplementoResponse::from)
                .toList();
    }

    @GetMapping("/activas")
    public List<EquivalenciaUnidadSuplementoResponse> activas(
            @PathVariable Long suplementoId, @RequestParam LocalDate fecha) {
        return service.listarActivas(suplementoId, fecha).stream()
                .map(EquivalenciaUnidadSuplementoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquivalenciaUnidadSuplementoResponse crear(
            @PathVariable Long suplementoId,
            @Valid @RequestBody EquivalenciaUnidadSuplementoRequest r) {
        return EquivalenciaUnidadSuplementoResponse.from(service.crear(suplementoId, r));
    }
}
