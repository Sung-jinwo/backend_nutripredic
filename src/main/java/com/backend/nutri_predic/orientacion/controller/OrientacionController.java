package com.backend.nutri_predic.orientacion.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.orientacion.dto.OrientacionResponse;
import com.backend.nutri_predic.orientacion.service.OrientacionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrientacionController {
    private final OrientacionService service;
    private final AccessService access;

    public OrientacionController(OrientacionService service, AccessService access) {
        this.service = service;
        this.access = access;
    }

    @GetMapping("/api/clientes/{clienteId}/orientacion")
    public OrientacionResponse orientacion(@PathVariable Long clienteId, Authentication authentication) {
        access.client(clienteId, authentication);
        return service.orientacion(clienteId);
    }
}
