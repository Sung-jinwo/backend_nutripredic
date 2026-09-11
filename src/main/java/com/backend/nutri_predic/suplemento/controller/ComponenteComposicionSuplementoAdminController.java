package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.service.ComposicionSuplementoService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/suplementos/composiciones/{composicionId}/componentes")
@PreAuthorize("hasRole('ADMIN')")
public class ComponenteComposicionSuplementoAdminController {
    private final ComposicionSuplementoService service;

    public ComponenteComposicionSuplementoAdminController(ComposicionSuplementoService s) {
        service = s;
    }

    @GetMapping
    public List<ComponenteComposicionSuplementoResponse> listar(@PathVariable Long composicionId) {
        return service.listarComponentes(composicionId).stream()
                .map(ComponenteComposicionSuplementoResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComponenteComposicionSuplementoResponse crear(
            @PathVariable Long composicionId,
            @Valid @RequestBody ComponenteComposicionSuplementoRequest r) {
        return ComponenteComposicionSuplementoResponse.from(
                service.agregarComponente(composicionId, r));
    }
}
