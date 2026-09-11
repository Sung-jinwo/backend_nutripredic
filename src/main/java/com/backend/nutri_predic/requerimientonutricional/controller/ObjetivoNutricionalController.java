package com.backend.nutri_predic.requerimientonutricional.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.requerimientonutricional.service.ObjetivoNutricionalService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class ObjetivoNutricionalController {
    private final ObjetivoNutricionalService service;
    private final AccessService access;

    public ObjetivoNutricionalController(ObjetivoNutricionalService service, AccessService access) {
        this.service = service; this.access = access;
    }

    @GetMapping("/api/clientes/{clienteId}/objetivo-nutricional")
    public ResponseEntity<?> objetivo(@PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication auth) {
        access.client(clienteId, auth);
        return ResponseEntity.ok(service.resolver(clienteId, fecha));
    }
}
