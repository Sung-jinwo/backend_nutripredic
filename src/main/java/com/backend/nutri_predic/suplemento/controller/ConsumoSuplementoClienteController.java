package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.suplemento.dto.RegistroConsumoSuplementoClienteRequest;
import com.backend.nutri_predic.suplemento.dto.RegistroConsumoSuplementoResponse;
import com.backend.nutri_predic.suplemento.service.ConsumoSuplementoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumoSuplementoClienteController {
    private final ConsumoSuplementoService service;

    public ConsumoSuplementoClienteController(ConsumoSuplementoService service) {
        this.service = service;
    }

    @PostMapping("/api/clientes/{clienteId}/suplementos/consumo")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroConsumoSuplementoResponse crear(
            @PathVariable Long clienteId,
            @Valid @RequestBody RegistroConsumoSuplementoClienteRequest request,
            Authentication authentication) {
        return service.crearPorClienteYFecha(clienteId, request, authentication);
    }
}
