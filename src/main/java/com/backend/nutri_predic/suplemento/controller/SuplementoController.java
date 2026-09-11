package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.service.SuplementoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class SuplementoController {
    private final SuplementoService service;

    public SuplementoController(SuplementoService service) {
        this.service = service;
    }

    @GetMapping("/api/suplementos")
    @Operation(summary = "Listar el catálogo de suplementos")
    public List<SuplementoCatalogoResponse> catalog() {
        return service.catalog();
    }

    @PostMapping("/api/suplementos")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SuplementoCatalogoResponse create(@Valid @RequestBody SuplementoCatalogoRequest r) {
        return service.saveCatalog(null, r);
    }

    @PutMapping("/api/suplementos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SuplementoCatalogoResponse updateCatalog(
            @PathVariable Long id, @Valid @RequestBody SuplementoCatalogoRequest r) {
        return service.saveCatalog(id, r);
    }

    @DeleteMapping("/api/suplementos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCatalog(@PathVariable Long id) {
        service.deleteCatalog(id);
    }

    @GetMapping("/api/clientes/{clienteId}/suplementos")
    @Operation(summary = "Listar suplementos asignados a un cliente")
    public List<SuplementoClienteResponse> list(@PathVariable Long clienteId, Authentication auth) {
        return service.list(clienteId, auth);
    }

    @GetMapping("/api/clientes/{clienteId}/suplementos/habituales")
    @Operation(summary = "Listar suplementos habituales activos y aplicables en una fecha")
    public List<SuplementoClienteResponse> habituales(
            @PathVariable Long clienteId,
            @RequestParam java.time.LocalDate fecha,
            Authentication auth) {
        return service.habituales(clienteId, fecha, auth);
    }

    @PostMapping("/api/clientes/{clienteId}/suplementos")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Asignar un suplemento")
    public SuplementoClienteResponse assign(
            @PathVariable Long clienteId,
            @Valid @RequestBody SuplementoAsignacionRequest r,
            Authentication auth) {
        return service.assign(clienteId, r, auth);
    }

    @PutMapping("/api/clientes/{clienteId}/suplementos/{suplementoId}")
    @Operation(summary = "Actualizar un suplemento asignado")
    public SuplementoClienteResponse update(
            @PathVariable Long clienteId,
            @PathVariable Long suplementoId,
            @Valid @RequestBody SuplementoActualizacionRequest r,
            Authentication auth) {
        return service.update(clienteId, suplementoId, r, auth);
    }

    @DeleteMapping("/api/clientes/{clienteId}/suplementos/{suplementoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un suplemento asignado")
    public void delete(
            @PathVariable Long clienteId, @PathVariable Long suplementoId, Authentication auth) {
        service.remove(clienteId, suplementoId, auth);
    }

}
