package com.backend.nutri_predic.cliente.dto;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.common.enums.TipoEntrenamiento;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.common.enums.SexoBiologico;
import java.math.BigDecimal;

public record ClienteResponse(
        Long id,
        Long usuarioId,
        String email,
        String nombre,
        Integer edad,
        SexoBiologico sexo,
        BigDecimal pesoKg,
        BigDecimal alturaCm,
        BigDecimal imc,
        String objetivoFisico,
        TipoObjetivoFisico tipoObjetivoFisico,
        Boolean realizaActividadFisica,
        Integer diasEntrenamientoSemana,
        String tipoActividadFisica,
        TipoEntrenamiento tipoEntrenamiento,
        Integer duracionPromedioSesionMinutos,
        ObjetivoEnergetico objetivoEnergetico,
        String estado) {
    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getUsuario().getId(),
                cliente.getUsuario().getEmail(),
                cliente.getUsuario().getNombre(),
                cliente.getEdad(),
                cliente.getSexo(),
                cliente.getPesoKg(),
                cliente.getAlturaCm(),
                cliente.getImc(),
                cliente.getObjetivoFisico(),
                cliente.getTipoObjetivoFisico(),
                cliente.getRealizaActividadFisica(),
                cliente.getDiasEntrenamientoSemana(),
                cliente.getTipoActividadFisica(),
                cliente.getTipoEntrenamiento(),
                cliente.getDuracionPromedioSesionMinutos(),
                cliente.getObjetivoEnergetico(),
                cliente.getEstado().name());
    }
}
