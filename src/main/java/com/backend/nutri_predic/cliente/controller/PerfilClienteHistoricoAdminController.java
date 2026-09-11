package com.backend.nutri_predic.cliente.controller;

import com.backend.nutri_predic.cliente.dto.PerfilClienteHistoricoResponse;
import com.backend.nutri_predic.cliente.service.PerfilClienteHistoricoService;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/clientes/{clienteId}/perfil-historico")
@PreAuthorize("hasRole('ADMIN')")
public class PerfilClienteHistoricoAdminController {
    private final PerfilClienteHistoricoService service;

    public PerfilClienteHistoricoAdminController(PerfilClienteHistoricoService service) {
        this.service = service;
    }

    @GetMapping
    public PerfilClienteHistoricoResponse obtener(
            @PathVariable Long clienteId, @RequestParam LocalDate fechaCorte) {
        return service.resolver(clienteId, fechaCorte);
    }
}
