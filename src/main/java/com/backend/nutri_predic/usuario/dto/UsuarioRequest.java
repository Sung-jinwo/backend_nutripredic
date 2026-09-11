package com.backend.nutri_predic.usuario.dto;

import jakarta.validation.constraints.Size;

public record UsuarioRequest(@Size(min = 2, max = 120) String nombre, Boolean activo) {}
