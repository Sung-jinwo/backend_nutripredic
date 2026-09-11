package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.service.ConsumoSuplementoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/habitos/{habitoId}/consumos-suplementos")
public class ConsumoSuplementoController {
    private final ConsumoSuplementoService service;

    public ConsumoSuplementoController(ConsumoSuplementoService s) {
        service = s;
    }

    @GetMapping
    public List<RegistroConsumoSuplementoResponse> listar(
            @PathVariable Long habitoId, Authentication a) {
        return service.listar(habitoId, a);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroConsumoSuplementoResponse crear(
            @PathVariable Long habitoId,
            @Valid @RequestBody RegistroConsumoSuplementoRequest r,
            Authentication a) {
        return service.crear(habitoId, r, a);
    }

    @PutMapping("/{id}")
    public RegistroConsumoSuplementoResponse editar(
            @PathVariable Long habitoId,
            @PathVariable Long id,
            @Valid @RequestBody RegistroConsumoSuplementoRequest r,
            Authentication a) {
        return service.actualizar(habitoId, id, r, a);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long habitoId, @PathVariable Long id, Authentication a) {
        service.eliminar(habitoId, id, a);
    }
}
