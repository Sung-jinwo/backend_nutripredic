package com.backend.nutri_predic.suplemento.controller;

import com.backend.nutri_predic.suplemento.dto.ResumenSuplementacionResponse;
import com.backend.nutri_predic.suplemento.service.AgregacionSuplementacionService;
import java.time.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/clientes/{clienteId}/suplementacion")
@PreAuthorize("hasRole('ADMIN')")
public class AgregacionSuplementacionAdminController {
    private final AgregacionSuplementacionService service;

    public AgregacionSuplementacionAdminController(AgregacionSuplementacionService s) {
        service = s;
    }

    @GetMapping("/resumen")
    public ResumenSuplementacionResponse resumen(
            @PathVariable Long clienteId, @RequestParam LocalDate fechaCorte) {
        return service.resumir(clienteId, fechaCorte);
    }
}
