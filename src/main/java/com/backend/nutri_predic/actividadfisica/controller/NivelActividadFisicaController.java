package com.backend.nutri_predic.actividadfisica.controller;

import com.backend.nutri_predic.actividadfisica.service.NivelActividadFisicaService;
import com.backend.nutri_predic.common.service.AccessService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class NivelActividadFisicaController {
    private final NivelActividadFisicaService service; private final AccessService access;
    public NivelActividadFisicaController(NivelActividadFisicaService service, AccessService access){this.service=service;this.access=access;}
    @GetMapping("/api/clientes/{clienteId}/nivel-actividad")
    public ResponseEntity<?> nivel(@PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            Authentication auth){
        access.client(clienteId, auth);
        return ResponseEntity.ok(service.resolver(clienteId, fechaCorte));
    }
}
