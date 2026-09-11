package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.service.ComposicionSuplementoService;
import jakarta.validation.Valid;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/suplementos/{suplementoId}/composiciones")
@PreAuthorize("hasRole('ADMIN')")
public class ComposicionSuplementoAdminController {
    private final ComposicionSuplementoService service;

    public ComposicionSuplementoAdminController(ComposicionSuplementoService s) {
        service = s;
    }

    @GetMapping
    public List<ComposicionSuplementoResponse> listar(@PathVariable Long suplementoId) {
        return service.listarPorSuplemento(suplementoId).stream()
                .map(ComposicionSuplementoResponse::from)
                .toList();
    }

    @GetMapping("/activa")
    public ComposicionSuplementoResponse activa(
            @PathVariable Long suplementoId, @RequestParam LocalDate fecha) {
        return ComposicionSuplementoResponse.from(
                service.obtenerActiva(suplementoId, fecha)
                        .orElseThrow(() -> new ResourceNotFoundException("Composición activa")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComposicionSuplementoResponse crear(
            @PathVariable Long suplementoId, @Valid @RequestBody ComposicionSuplementoRequest r) {
        return ComposicionSuplementoResponse.from(service.crear(suplementoId, r));
    }
}
