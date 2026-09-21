package com.backend.nutri_predic.conocimiento.practica.controller;

import com.backend.nutri_predic.conocimiento.practica.service.ConocimientoInicialService;
import com.backend.nutri_predic.conocimiento.practica.dto.SesionConocimientoPublicaResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/clientes/{clienteId}/conocimiento")
public class ConocimientoInicialController {
    private final ConocimientoInicialService service;
    public ConocimientoInicialController(ConocimientoInicialService service) { this.service = service; }
    @PostMapping("/diario-perfil/asegurar")
    public SesionConocimientoPublicaResponse asegurarDiaria(@PathVariable Long clienteId, Authentication auth) {
        return service.asegurarDiaria(clienteId, auth);
    }
    @PostMapping("/inicial/asegurar")
    public SesionConocimientoPublicaResponse asegurar(@PathVariable Long clienteId, Authentication auth) {
        return service.asegurar(clienteId, auth);
    }
}
