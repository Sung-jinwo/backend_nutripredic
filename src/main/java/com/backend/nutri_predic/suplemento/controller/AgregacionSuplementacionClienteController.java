package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.suplemento.dto.ResumenSuplementacionResponse;
import com.backend.nutri_predic.suplemento.service.AgregacionSuplementacionService;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes/{clienteId}/suplementacion")
public class AgregacionSuplementacionClienteController {
    private final AccessService access;
    private final AgregacionSuplementacionService service;

    public AgregacionSuplementacionClienteController(
            AccessService access, AgregacionSuplementacionService service) {
        this.access = access;
        this.service = service;
    }

    @GetMapping("/resumen")
    public ResumenSuplementacionResponse resumen(
            @PathVariable Long clienteId,
            @RequestParam LocalDate fechaCorte,
            Authentication authentication) {
        access.client(clienteId, authentication);
        return service.resumir(clienteId, fechaCorte);
    }
}
