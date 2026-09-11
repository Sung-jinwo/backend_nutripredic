package com.backend.nutri_predic.usuario.controller;

import com.backend.nutri_predic.usuario.dto.PerfilResponse;
import com.backend.nutri_predic.usuario.dto.UsuarioRequest;
import com.backend.nutri_predic.usuario.dto.UsuarioResponse;
import com.backend.nutri_predic.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public PerfilResponse me(Authentication authentication) {
        return service.current(authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse update(
            @PathVariable Long id, @Valid @RequestBody UsuarioRequest request) {
        return service.update(id, request);
    }
}
