package com.backend.nutri_predic.auth.dto;

import com.backend.nutri_predic.common.enums.SexoBiologico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El email es obligatorio")
                @Email(message = "El email debe tener un formato válido")
                @Size(max = 254, message = "El email no puede superar 254 caracteres")
                String email,
        @NotBlank(message = "La contraseña es obligatoria")
                @Size(
                        min = 8,
                        max = 72,
                        message = "La contraseña debe tener entre 8 y 72 caracteres")
                String password,
        @NotBlank(message = "El nombre es obligatorio")
                @Size(min = 2, max = 120, message = "El nombre debe tener entre 2 y 120 caracteres")
                String nombre,
        SexoBiologico sexo,
        Boolean realizaActividadFisica,
        @Min(value = 1, message = "Mínimo 1 día de entrenamiento a la semana")
                @Max(value = 7, message = "Máximo 7 días de entrenamiento a la semana")
                Integer diasEntrenamientoSemana,
        @Size(max = 120, message = "El tipo de actividad no puede superar 120 caracteres")
                String tipoActividadFisica) {
    /**
     * Paso 1 del registro: solo credenciales y nombre. El perfil (edad, peso,
     * altura, objetivo, actividad) se completa después vía PUT /api/clientes/{id}.
     */
    public RegisterRequest(String email, String password, String nombre) {
        this(email, password, nombre, null, null, null, null);
    }
}
