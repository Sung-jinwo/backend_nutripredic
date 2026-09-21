package com.backend.nutri_predic.cliente.peso.controller;

import com.backend.nutri_predic.cliente.peso.dto.*;
import com.backend.nutri_predic.cliente.peso.service.RegistroPesoClienteService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{clienteId}/peso-semanal")
public class RegistroPesoClienteController {
    private final RegistroPesoClienteService service;
    public RegistroPesoClienteController(RegistroPesoClienteService service) { this.service = service; }
    @GetMapping("/historial") public java.util.List<RegistroPesoResponse> historial(@PathVariable Long clienteId, Authentication auth) {
        return service.historial(clienteId, auth);
    }
    @GetMapping public EstadoPesoSemanalResponse estado(@PathVariable Long clienteId, Authentication auth) {
        return service.estado(clienteId, auth);
    }
    @PostMapping public EstadoPesoSemanalResponse registrar(@PathVariable Long clienteId,
            @Valid @RequestBody RegistroPesoRequest request, Authentication auth) {
        return service.registrar(clienteId, request, auth);
    }
}
