package com.backend.nutri_predic.conocimiento.practica.controller;

import com.backend.nutri_predic.conocimiento.practica.dto.*;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/conocimiento-ia/sesiones")
public class AdminConocimientoIaController {
    private final GeneracionPreguntasConocimientoService service;

    public AdminConocimientoIaController(GeneracionPreguntasConocimientoService s) {
        service = s;
    }

    @PostMapping("/{sesionId}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SesionConocimientoPublicaResponse retry(
            @PathVariable Long sesionId,
            @Valid @RequestBody GenerarConocimientoIaRequest request,
            Authentication auth) {
        return service.generarRetryAdministrativo(sesionId, request, auth);
    }
}
