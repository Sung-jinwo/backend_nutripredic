package com.backend.nutri_predic.cliente.dto;

import com.backend.nutri_predic.cliente.entity.HistorialPerfilCliente;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.common.enums.SexoBiologico;
import java.math.BigDecimal;
import java.time.*;

public record HistorialPerfilResponse(
        Long id,
        Integer edad,
        SexoBiologico sexo,
        BigDecimal pesoKg,
        BigDecimal alturaCm,
        BigDecimal imc,
        String objetivoFisico,
        TipoObjetivoFisico tipoObjetivoFisico,
        LocalDate fechaDesde,
        Instant creadoEn) {
    public static HistorialPerfilResponse from(HistorialPerfilCliente h) {
        return new HistorialPerfilResponse(
                h.getId(),
                h.getEdad(),
                h.getSexo(),
                h.getPesoKg(),
                h.getAlturaCm(),
                h.getImc(),
                h.getObjetivoFisico(),
                h.getTipoObjetivoFisico(),
                h.getFechaDesde(),
                h.getCreadoEn());
    }
}
