package com.backend.nutri_predic.usuario.dto;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.common.enums.SexoBiologico;
import com.backend.nutri_predic.common.enums.TipoEntrenamiento;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.usuario.entity.Usuario;
import java.math.BigDecimal;

public record PerfilResponse(
        Long id,
        Long clienteId,
        String email,
        String nombre,
        String rol,
        boolean activo,
        Integer edad,
        BigDecimal pesoKg,
        BigDecimal alturaCm,
        BigDecimal imc,
        String objetivoFisico,
        TipoObjetivoFisico tipoObjetivoFisico,
        SexoBiologico sexo,
        Boolean realizaActividadFisica,
        Integer diasEntrenamientoSemana,
        String tipoActividadFisica,
        TipoEntrenamiento tipoEntrenamiento,
        Integer duracionPromedioSesionMinutos,
        ObjetivoEnergetico objetivoEnergetico) {

    public static PerfilResponse from(Usuario usuario, Cliente cliente) {
        return new PerfilResponse(
                usuario.getId(),
                cliente == null ? null : cliente.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name(),
                usuario.isActivo(),
                cliente == null ? null : cliente.getEdad(),
                cliente == null ? null : cliente.getPesoKg(),
                cliente == null ? null : cliente.getAlturaCm(),
                cliente == null ? null : cliente.getImc(),
                cliente == null ? null : cliente.getObjetivoFisico(),
                cliente == null ? null : cliente.getTipoObjetivoFisico(),
                cliente == null ? null : cliente.getSexo(),
                cliente == null ? null : cliente.getRealizaActividadFisica(),
                cliente == null ? null : cliente.getDiasEntrenamientoSemana(),
                cliente == null ? null : cliente.getTipoActividadFisica(),
                cliente == null ? null : cliente.getTipoEntrenamiento(),
                cliente == null ? null : cliente.getDuracionPromedioSesionMinutos(),
                cliente == null ? null : cliente.getObjetivoEnergetico());
    }
}
