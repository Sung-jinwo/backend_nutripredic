package com.backend.nutri_predic.prediccionmodelo.ciclo.controller;

import com.backend.nutri_predic.prediccionmodelo.ciclo.dto.CicloDiarioResponse;
import com.backend.nutri_predic.prediccionmodelo.ciclo.service.CicloDiarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{clienteId}/ciclo-diario")
public class CicloDiarioController {
    private final CicloDiarioService service;
    public CicloDiarioController(CicloDiarioService service) { this.service = service; }
    @PostMapping("/asegurar")
    public CicloDiarioResponse asegurar(@PathVariable Long clienteId, Authentication auth) {
        return service.asegurar(clienteId, auth);
    }

    @GetMapping("/estado")
    public CicloDiarioResponse estado(@PathVariable Long clienteId, Authentication auth) {
        return service.estado(clienteId, auth);
    }
}
