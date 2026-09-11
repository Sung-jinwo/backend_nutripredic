package com.backend.nutri_predic.perfilhabitos.controller;

import com.backend.nutri_predic.perfilhabitos.dto.*;
import com.backend.nutri_predic.perfilhabitos.service.EvidenciaPerfilHabitosV6Service;
import com.backend.nutri_predic.perfilhabitos.service.EvaluacionPerfilHabitosService;
import com.backend.nutri_predic.perfilhabitos.service.EvaluacionPerfilHabitosV6Service;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/evaluaciones-perfil")
public class EvaluacionPerfilHabitosController {
    private final EvaluacionPerfilHabitosService service;
    private final EvidenciaPerfilHabitosV6Service evidenciaV6;
    private final EvaluacionPerfilHabitosV6Service evaluacionV6;

    public EvaluacionPerfilHabitosController(
            EvaluacionPerfilHabitosService service,
            EvidenciaPerfilHabitosV6Service evidenciaV6,
            EvaluacionPerfilHabitosV6Service evaluacionV6) {
        this.service = service;
        this.evidenciaV6 = evidenciaV6;
        this.evaluacionV6 = evaluacionV6;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluacionPerfilHabitosResponse crear(
            @Valid @RequestBody EvaluacionPerfilHabitosRequest r, Authentication a) {
        return service.crear(r, a);
    }

    @GetMapping
    public List<EvaluacionPerfilHabitosResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EvaluacionPerfilHabitosResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/{id}/detalle")
    public DetalleEvaluacionPerfilHabitosResponse detalle(@PathVariable Long id) {
        return evaluacionV6.detalle(id);
    }

    @GetMapping("/candidata-v6/clientes/{clienteId}/evidencia")
    public EvidenciaPerfilHabitosV6Response evidenciaCandidataV6(
            @PathVariable Long clienteId, @RequestParam java.time.LocalDate fechaCorte) {
        return evidenciaV6.calcular(clienteId, fechaCorte);
    }

    @PostMapping("/candidata-v6/clientes/{clienteId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DetalleEvaluacionPerfilHabitosResponse crearCandidataV6(
            @PathVariable Long clienteId,
            @RequestParam java.time.LocalDate fechaCorte,
            @Valid @RequestBody CrearEvaluacionPerfilHabitosV6Request request,
            Authentication authentication) {
        return evaluacionV6.crearDesdeEvidencia(clienteId, fechaCorte, request, authentication);
    }
}
