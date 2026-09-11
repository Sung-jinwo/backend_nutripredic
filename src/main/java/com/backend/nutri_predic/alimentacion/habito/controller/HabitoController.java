package com.backend.nutri_predic.alimentacion.habito.controller;

import com.backend.nutri_predic.alimentacion.habito.dto.*;
import com.backend.nutri_predic.alimentacion.habito.service.HabitoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habitos")
public class HabitoController {
    private final HabitoService service;

    public HabitoController(HabitoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un registro diario de hábitos")
    public HabitoResponse create(@Valid @RequestBody HabitoRequest request, Authentication auth) {
        return service.create(request, auth);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar los hábitos de un cliente")
    public List<HabitoResponse> list(@PathVariable Long clienteId, Authentication auth) {
        return service.list(clienteId, auth);
    }

    @GetMapping("/cliente/{clienteId}/{fecha}")
    @Operation(summary = "Obtener los hábitos de un cliente por fecha")
    public HabitoResponse get(
            @PathVariable Long clienteId, @PathVariable LocalDate fecha, Authentication auth) {
        return service.get(clienteId, fecha, auth);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Reemplazar un registro de hábitos")
    public HabitoResponse update(
            @PathVariable Long id,
            @Valid @RequestBody HabitoUpdateRequest request,
            Authentication auth) {
        return service.update(id, request, auth);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un registro de hábitos")
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth);
    }
}
