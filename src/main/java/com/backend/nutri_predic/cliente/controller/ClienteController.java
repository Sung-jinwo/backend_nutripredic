package com.backend.nutri_predic.cliente.controller;

import com.backend.nutri_predic.cliente.dto.ClienteRequest;
import com.backend.nutri_predic.cliente.dto.ClienteResponse;
import com.backend.nutri_predic.cliente.dto.HistorialPerfilResponse;
import com.backend.nutri_predic.cliente.service.ClienteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClienteResponse> listar() {
        return service.list();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtener(@PathVariable Long id, Authentication authentication) {
        return service.get(id, authentication);
    }

    @GetMapping("/{id}/estado")
    public ClienteResponse estado(@PathVariable Long id, Authentication authentication) {
        return service.get(id, authentication);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest body,
            Authentication authentication) {
        return service.update(id, body, authentication);
    }

    @GetMapping("/{id}/historial-perfil")
    public List<HistorialPerfilResponse> historialPerfil(
            @PathVariable Long id, Authentication authentication) {
        return service.historial(id, authentication);
    }

    @GetMapping("/{id}/historial-perfil/{fecha}")
    public HistorialPerfilResponse perfilEnFecha(
            @PathVariable Long id,
            @PathVariable java.time.LocalDate fecha,
            Authentication authentication) {
        return service.perfilEnFecha(id, fecha, authentication);
    }
}
