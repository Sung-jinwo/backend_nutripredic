package com.backend.nutri_predic.suplemento.dto;

import jakarta.validation.constraints.*;

public record SuplementoCatalogoRequest(
        @NotBlank @Size(max = 150) String nombre,
        @NotBlank @Size(max = 100) String tipo,
        @Size(max = 100) String tipoSuplemento,
        @Size(max = 2000) String descripcion,
        @Size(max = 2000) String beneficios,
        @Size(max = 2000) String recomendaciones,
        @Size(max = 150) String marca,
        @Size(max = 150) String presentacion,
        @Size(max = 30) String unidadPresentacionCodigo,
        Boolean activo) {
    public SuplementoCatalogoRequest {
        if ((tipo == null || tipo.isBlank()) && tipoSuplemento != null) tipo = tipoSuplemento;
    }

    public SuplementoCatalogoRequest(
            String nombre,
            String tipo,
            String descripcion,
            String beneficios,
            String recomendaciones,
            String marca,
            String presentacion,
            String unidadPresentacionCodigo,
            Boolean activo) {
        this(
                nombre,
                tipo,
                null,
                descripcion,
                beneficios,
                recomendaciones,
                marca,
                presentacion,
                unidadPresentacionCodigo,
                activo);
    }
}
