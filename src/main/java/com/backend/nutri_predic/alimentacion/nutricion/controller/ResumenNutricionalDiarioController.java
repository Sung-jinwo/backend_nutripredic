package com.backend.nutri_predic.alimentacion.nutricion.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.alimentacion.nutricion.service.ResumenNutricionalDiarioService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class ResumenNutricionalDiarioController {
    private final ResumenNutricionalDiarioService service;
    private final AccessService access;
    public ResumenNutricionalDiarioController(ResumenNutricionalDiarioService service, AccessService access){this.service=service;this.access=access;}
    @GetMapping("/api/clientes/{clienteId}/resumen-diario")
    public ResponseEntity<?> resumen(@PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication auth){
        access.client(clienteId, auth);
        return ResponseEntity.ok(service.resumen(clienteId, fecha));
    }
}
