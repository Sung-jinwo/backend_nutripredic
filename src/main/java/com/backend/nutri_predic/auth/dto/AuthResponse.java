package com.backend.nutri_predic.auth.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long usuarioId,
        Long clienteId,
        String email,
        String nombre,
        String rol) {}
