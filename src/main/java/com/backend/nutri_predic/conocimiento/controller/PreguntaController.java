package com.backend.nutri_predic.conocimiento.controller;

import com.backend.nutri_predic.conocimiento.dto.*;
import com.backend.nutri_predic.conocimiento.service.ConocimientoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {
    private final ConocimientoService service;

    public PreguntaController(ConocimientoService s) {
        service = s;
    }

    @GetMapping
    public List<PreguntaPublicaResponse> list() {
        return service.listPublic();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PreguntaAdminResponse> admin() {
        return service.listAdmin();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public PreguntaAdminResponse create(@Valid @RequestBody PreguntaRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PreguntaAdminResponse update(
            @PathVariable Long id, @Valid @RequestBody PreguntaRequest r) {
        return service.createVersion(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deactivate(id);
    }
}
