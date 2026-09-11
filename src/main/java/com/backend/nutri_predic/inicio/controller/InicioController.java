package com.backend.nutri_predic.inicio.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.inicio.service.InicioService;
import com.backend.nutri_predic.inicio.service.SiguienteAccionService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class InicioController {
    private final InicioService inicio; private final SiguienteAccionService accion; private final AccessService access;
    public InicioController(InicioService inicio, SiguienteAccionService accion, AccessService access){this.inicio=inicio;this.accion=accion;this.access=access;}
    @GetMapping("/api/clientes/{clienteId}/inicio")
    public ResponseEntity<?> inicio(@PathVariable Long clienteId, @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha, Authentication auth){
        access.client(clienteId, auth);
        return ResponseEntity.ok(inicio.inicio(clienteId, fecha));
    }
    @GetMapping("/api/clientes/{clienteId}/siguiente-accion")
    public ResponseEntity<?> siguiente(@PathVariable Long clienteId, @RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate fecha, Authentication auth){
        access.client(clienteId, auth);
        return ResponseEntity.ok(java.util.Map.of("accion", accion.resolver(clienteId, fecha)));
    }
}
