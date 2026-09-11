package com.backend.nutri_predic.consumo.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.consumo.dto.*;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.repository.DetalleEvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.service.ConsumoEvaluacionExtractor;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluaciones-consumo")
public class EvaluacionConsumoController {
    private final ConsumoEvaluacionExtractor extractor;
    private final EvaluacionConsumoRepository repo;
    private final AccessService access;
    private final DetalleEvaluacionConsumoRepository detalles;

    public EvaluacionConsumoController(
            ConsumoEvaluacionExtractor e, EvaluacionConsumoRepository r, AccessService a, DetalleEvaluacionConsumoRepository d) {
        extractor = e;
        repo = r;
        access = a;
        detalles = d;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EvaluacionConsumoResponse crear(
            @Valid @RequestBody EvaluacionConsumoRequest r, Authentication a) {
        access.client(r.clienteId(), a);
        return extractor.extraer(r);
    }

    @GetMapping("/{id}")
    public EvaluacionConsumoResponse get(@PathVariable Long id, Authentication a) {
        var e = repo.findById(id).orElseThrow();
        access.client(e.getCliente().getId(), a);
        return EvaluacionConsumoResponse.from(e);
    }

    @GetMapping("/{id}/detalles")
    public List<DetalleEvaluacionConsumoResponse> detalles(@PathVariable Long id, Authentication a) {
        var e = repo.findById(id).orElseThrow();
        access.client(e.getCliente().getId(), a);
        return detalles.findByEvaluacionIdOrderByIdAsc(id).stream().map(DetalleEvaluacionConsumoResponse::from).toList();
    }
}
