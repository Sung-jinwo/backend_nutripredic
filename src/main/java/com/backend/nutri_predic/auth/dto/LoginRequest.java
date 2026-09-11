package com.backend.nutri_predic.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio")
                @Email(message = "El email debe tener un formato válido")
                @Size(max = 254, message = "El email no puede superar 254 caracteres")
                String email,
        @NotBlank(message = "La contraseña es obligatoria")
                @Size(max = 72, message = "La contraseña no puede superar 72 caracteres")
                String password) {}
