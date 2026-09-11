package com.backend.nutri_predic.perfilhabitos.controller;

import com.backend.nutri_predic.perfilhabitos.dto.*;
import com.backend.nutri_predic.perfilhabitos.service.RubricaPerfilHabitosService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/rubricas-perfil")
public class RubricaPerfilHabitosController {
    private final RubricaPerfilHabitosService service;

    public RubricaPerfilHabitosController(RubricaPerfilHabitosService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RubricaPerfilHabitosResponse crear(@Valid @RequestBody RubricaPerfilHabitosRequest r) {
        return service.crear(r);
    }

    @GetMapping
    public List<RubricaPerfilHabitosResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RubricaPerfilHabitosResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PatchMapping("/{id}/estado")
    public RubricaPerfilHabitosResponse estado(
            @PathVariable Long id, @Valid @RequestBody CambioEstadoRubricaRequest r) {
        return service.cambiarEstado(id, r);
    }

    @PostMapping("/{id}/criterios")
    @ResponseStatus(HttpStatus.CREATED)
    public CriterioClasificacionPerfilResponse criterio(
            @PathVariable Long id, @Valid @RequestBody CriterioClasificacionPerfilRequest r) {
        return service.agregarCriterio(id, r);
    }

    @GetMapping("/{id}/criterios")
    public List<CriterioClasificacionPerfilResponse> criterios(@PathVariable Long id) {
        return service.criterios(id);
    }

    @PostMapping("/{id}/dimensiones")
    @ResponseStatus(HttpStatus.CREATED)
    public DimensionRubricaPerfilHabitosResponse dimension(
            @PathVariable Long id,
            @Valid @RequestBody DimensionRubricaPerfilHabitosRequest request) {
        return service.agregarDimension(id, request);
    }

    @PutMapping("/{id}/dimensiones/{dimensionId}")
    public DimensionRubricaPerfilHabitosResponse actualizarDimension(
            @PathVariable Long id,
            @PathVariable Long dimensionId,
            @Valid @RequestBody DimensionRubricaPerfilHabitosRequest request) {
        return service.actualizarDimension(id, dimensionId, request);
    }

    @GetMapping("/{id}/dimensiones")
    public List<DimensionRubricaPerfilHabitosResponse> dimensiones(@PathVariable Long id) {
        return service.dimensiones(id);
    }

    @PatchMapping("/{id}/validacion")
    public RubricaPerfilHabitosResponse validacion(
            @PathVariable Long id,
            @Valid @RequestBody ValidacionRubricaPerfilHabitosRequest request) {
        return service.registrarValidacion(id, request);
    }
}
