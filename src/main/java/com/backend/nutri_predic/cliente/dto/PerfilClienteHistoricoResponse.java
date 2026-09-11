package com.backend.nutri_predic.cliente.dto;

import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.common.enums.SexoBiologico;
import com.backend.nutri_predic.common.enums.TipoEntrenamiento;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PerfilClienteHistoricoResponse(
        Long clienteId,
        LocalDate fechaCorte,
        Integer edad,
        SexoBiologico sexo,
        BigDecimal pesoKg,
        BigDecimal alturaCm,
        BigDecimal imc,
        TipoObjetivoFisico tipoObjetivoFisico,
        String objetivoFisicoTexto,
        Boolean realizaActividadFisica,
        Integer diasEntrenamientoSemana,
        String tipoActividadFisica,
        TipoEntrenamiento tipoEntrenamiento,
        Integer duracionPromedioSesionMinutos,
        ObjetivoEnergetico objetivoEnergetico,
        String fuentePerfil,
        Instant fechaVersionAplicada) {}
