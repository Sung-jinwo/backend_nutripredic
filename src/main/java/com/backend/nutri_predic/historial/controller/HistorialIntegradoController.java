package com.backend.nutri_predic.historial.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.historial.service.HistorialIntegradoService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class HistorialIntegradoController {
    private final HistorialIntegradoService service; private final AccessService access;
    public HistorialIntegradoController(HistorialIntegradoService service, AccessService access){this.service=service;this.access=access;}
    @GetMapping("/api/clientes/{clienteId}/historial-integrado")
    public ResponseEntity<?> historial(@PathVariable Long clienteId,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate hasta,
            Authentication auth){
        access.client(clienteId, auth);
        return ResponseEntity.ok(service.historial(clienteId, desde, hasta));
    }
}
