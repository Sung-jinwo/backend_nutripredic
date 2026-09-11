package com.backend.nutri_predic.alimentacion.nutricion.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.alimentacion.nutricion.dto.ConsumoNutricionalDiarioResponse;
import com.backend.nutri_predic.alimentacion.nutricion.service.ConsumoNutricionalDiarioService;
import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clientes/{clienteId}/consumo-nutricional")
public class ConsumoNutricionalDiarioController {
    private final AccessService access;
    private final ConsumoNutricionalDiarioService service;
    public ConsumoNutricionalDiarioController(AccessService access, ConsumoNutricionalDiarioService service) {
        this.access = access; this.service = service;
    }
    @GetMapping("/diario")
    public ConsumoNutricionalDiarioResponse diario(@PathVariable Long clienteId, @RequestParam LocalDate fecha, Authentication auth) {
        access.client(clienteId, auth);
        return service.obtener(clienteId, fecha);
    }
}
