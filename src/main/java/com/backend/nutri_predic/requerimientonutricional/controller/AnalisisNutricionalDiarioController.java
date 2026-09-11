package com.backend.nutri_predic.requerimientonutricional.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.requerimientonutricional.dto.AnalisisNutricionalDiarioResponse;
import com.backend.nutri_predic.requerimientonutricional.service.AnalisisNutricionalDiarioService;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Comparación nutricional; no participa en PCC, PCS, TPP ni en inferencia ML. */
@RestController
@RequestMapping("/api/clientes/{clienteId}/analisis-nutricional")
public class AnalisisNutricionalDiarioController {
    private final AccessService access;
    private final AnalisisNutricionalDiarioService service;
    public AnalisisNutricionalDiarioController(AccessService access, AnalisisNutricionalDiarioService service) { this.access = access; this.service = service; }
    @GetMapping("/diario")
    public AnalisisNutricionalDiarioResponse diario(@PathVariable Long clienteId, @RequestParam LocalDate fecha, Authentication auth) {
        access.client(clienteId, auth);
        return service.analizar(clienteId, fecha);
    }
}
