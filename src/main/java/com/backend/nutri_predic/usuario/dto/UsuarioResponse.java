package com.backend.nutri_predic.usuario.dto;

import com.backend.nutri_predic.usuario.entity.Usuario;

public record UsuarioResponse(Long id, String email, String nombre, String rol, boolean activo) {
    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name(),
                usuario.isActivo());
    }
}
