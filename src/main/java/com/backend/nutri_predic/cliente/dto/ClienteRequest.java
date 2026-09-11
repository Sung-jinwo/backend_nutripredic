package com.backend.nutri_predic.cliente.dto;

import com.backend.nutri_predic.common.enums.EstadoCliente;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.common.enums.TipoEntrenamiento;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.common.enums.SexoBiologico;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ClienteRequest(
        @Min(1) @Max(120) Integer edad,
        SexoBiologico sexo,
        @DecimalMin(value = "1.0", message = "El peso debe ser de al menos 1 kg")
                @DecimalMax(value = "500.0", message = "El peso no puede superar 500 kg")
                BigDecimal pesoKg,
        @DecimalMin(value = "30.0", message = "La altura debe ser de al menos 30 cm")
                @DecimalMax(value = "300.0", message = "La altura no puede superar 300 cm")
                BigDecimal alturaCm,
        @Size(
                        min = 2,
                        max = 120,
                        message = "El objetivo físico debe tener entre 2 y 120 caracteres")
        String objetivoFisico,
        TipoObjetivoFisico tipoObjetivoFisico,
        Boolean realizaActividadFisica,
        @Min(1) @Max(7) Integer diasEntrenamientoSemana,
        @Size(max = 120) String tipoActividadFisica,
        TipoEntrenamiento tipoEntrenamiento,
        @Min(1) @Max(1440) Integer duracionPromedioSesionMinutos,
        ObjetivoEnergetico objetivoEnergetico,
        EstadoCliente estado) {
    /** Compatibilidad para constructores internos previos a la ampliación del perfil. */
    public ClienteRequest(
            Integer edad,
            BigDecimal pesoKg,
            BigDecimal alturaCm,
            String objetivoFisico,
            TipoObjetivoFisico tipoObjetivoFisico,
            EstadoCliente estado) {
        this(
                edad,
                null,
                pesoKg,
                alturaCm,
                objetivoFisico,
                tipoObjetivoFisico,
                null,
                null,
                null,
                null,
                null,
                null,
                estado);
    }
}
