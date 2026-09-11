package com.backend.nutri_predic.plandia.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.plandia.dto.PlanDiarioResponse;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{clienteId}")
public class PlanInicialController {
    private final AccessService access;
    private final PlanDiarioService service;

    public PlanInicialController(AccessService access, PlanDiarioService service) {
        this.access = access;
        this.service = service;
    }

    @PostMapping("/plan-diario/inicial")
    public PlanDiarioResponse inicial(
            @PathVariable Long clienteId,
            @RequestParam LocalDate fecha,
            Authentication authentication) {
        access.client(clienteId, authentication);
        return service.generarInicial(clienteId, fecha);
    }
}
