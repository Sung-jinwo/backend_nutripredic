package com.backend.nutri_predic.alimentacion.controller;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.service.AlimentacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AlimentacionController {
    private final AlimentacionService service;

    public AlimentacionController(AlimentacionService s) {
        service = s;
    }

    @GetMapping("/api/alimentos")
    public List<AlimentoCatalogoResponse> catalogo() {
        return service.catalogo();
    }

    @GetMapping("/api/alimentos/{id}")
    public AlimentoCatalogoResponse detalle(@PathVariable Long id) {
        return service.detalle(id);
    }

    @PostMapping("/api/alimentos")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public AlimentoCatalogoResponse crear(@Valid @RequestBody AlimentoCatalogoRequest r) {
        return service.guardarCatalogo(null, r);
    }

    @PutMapping("/api/alimentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AlimentoCatalogoResponse editar(
            @PathVariable Long id, @Valid @RequestBody AlimentoCatalogoRequest r) {
        return service.guardarCatalogo(id, r);
    }

    @DeleteMapping("/api/alimentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        service.desactivarCatalogo(id);
    }

    @GetMapping("/api/habitos/{habitoId}/alimentos")
    public List<RegistroAlimentoResponse> listar(@PathVariable Long habitoId, Authentication a) {
        return service.listarRegistro(habitoId, a);
    }

    @PostMapping("/api/habitos/{habitoId}/alimentos")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroAlimentoResponse agregar(
            @PathVariable Long habitoId,
            @Valid @RequestBody RegistroAlimentoRequest r,
            Authentication a) {
        return service.agregarRegistro(habitoId, r, a);
    }

    @PutMapping("/api/habitos/{habitoId}/alimentos/{id}")
    public RegistroAlimentoResponse editarRegistro(
            @PathVariable Long habitoId,
            @PathVariable Long id,
            @Valid @RequestBody RegistroAlimentoRequest r,
            Authentication a) {
        return service.actualizarRegistro(habitoId, id, r, a);
    }

    @DeleteMapping("/api/habitos/{habitoId}/alimentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable Long habitoId, @PathVariable Long id, Authentication a) {
        service.eliminarRegistro(habitoId, id, a);
    }

    @GetMapping("/api/clientes/{clienteId}/alimentos/recientes")
    public List<AlimentoUsoResponse> recientes(@PathVariable Long clienteId, Authentication a) {
        return service.recientes(clienteId, a);
    }

    @GetMapping("/api/clientes/{clienteId}/alimentos/frecuentes")
    public List<AlimentoUsoResponse> frecuentes(@PathVariable Long clienteId, Authentication a) {
        return service.frecuentes(clienteId, a);
    }

}
