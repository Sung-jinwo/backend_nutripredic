package com.backend.nutri_predic.consumo.controller;

import com.backend.nutri_predic.consumo.dto.CambioEstadoRubricaConsumoRequest;
import com.backend.nutri_predic.consumo.dto.CriterioConsumoSuplementosRequest;
import com.backend.nutri_predic.consumo.dto.CriterioConsumoSuplementosResponse;
import com.backend.nutri_predic.consumo.dto.RubricaConsumoSuplementosRequest;
import com.backend.nutri_predic.consumo.dto.RubricaConsumoSuplementosResponse;
import com.backend.nutri_predic.consumo.service.RubricaConsumoSuplementosService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pcs/rubricas")
public class AdminPcsRubricaController {
    private final RubricaConsumoSuplementosService service;

    public AdminPcsRubricaController(RubricaConsumoSuplementosService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RubricaConsumoSuplementosResponse crear(
            @Valid @RequestBody RubricaConsumoSuplementosRequest request) {
        return service.crear(request);
    }

    @GetMapping
    public List<RubricaConsumoSuplementosResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public RubricaConsumoSuplementosResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    public RubricaConsumoSuplementosResponse actualizar(
            @PathVariable Long id, @Valid @RequestBody RubricaConsumoSuplementosRequest request) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public RubricaConsumoSuplementosResponse cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambioEstadoRubricaConsumoRequest request) {
        return service.cambiarEstado(id, request);
    }

    @PostMapping("/{rubricaId}/criterios")
    @ResponseStatus(HttpStatus.CREATED)
    public CriterioConsumoSuplementosResponse agregarCriterio(
            @PathVariable Long rubricaId,
            @Valid @RequestBody CriterioConsumoSuplementosRequest request) {
        return service.agregarCriterio(rubricaId, request);
    }

    @PutMapping("/{rubricaId}/criterios/{criterioId}")
    public CriterioConsumoSuplementosResponse actualizarCriterio(
            @PathVariable Long rubricaId,
            @PathVariable Long criterioId,
            @Valid @RequestBody CriterioConsumoSuplementosRequest request) {
        return service.actualizarCriterio(rubricaId, criterioId, request);
    }

    @DeleteMapping("/{rubricaId}/criterios/{criterioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCriterio(@PathVariable Long rubricaId, @PathVariable Long criterioId) {
        service.eliminarCriterio(rubricaId, criterioId);
    }
}
