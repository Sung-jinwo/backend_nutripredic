package com.backend.nutri_predic.actividadfisica.service;

import com.backend.nutri_predic.actividadfisica.dto.NivelActividadFisicaResponse;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.common.enums.NivelActividadFisica;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Describe la actividad declarada en el perfil sin exigir cuestionarios adicionales. */
@Service
public class NivelActividadFisicaService {
    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final String REGLA_VERSION_PERFIL = "PERFIL_ESTRUCTURADO_V2";

    private final HistorialPerfilClienteRepository perfiles;

    public NivelActividadFisicaService(HistorialPerfilClienteRepository perfiles) {
        this.perfiles = perfiles;
    }

    @Transactional(readOnly = true)
    public NivelActividadFisicaResponse resolver(Long clienteId, LocalDate fechaCorte) {
        var perfil = perfiles
                .findFirstByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(clienteId, fechaCorte)
                .filter(p -> !p.getCreadoEn().isAfter(fechaCorte.plusDays(1).atStartOfDay(ZONA).toInstant()))
                .orElse(null);
        Boolean realiza = perfil != null ? perfil.getRealizaActividadFisica() : null;
        if (realiza == null) {
            return NivelActividadFisicaResponse.noDeterminada(
                    "PERFIL_INCOMPLETO", "Falta indicar si realiza actividad física", REGLA_VERSION_PERFIL);
        }
        if (Boolean.FALSE.equals(realiza)) {
            return new NivelActividadFisicaResponse(
                    NivelActividadFisica.SEDENTARIO, "PERFIL_ESTRUCTURADO", fechaCorte, null,
                    REGLA_VERSION_PERFIL, "Actividad declarada por el cliente", "V2",
                    "DECLARADA", true, null);
        }
        return NivelActividadFisicaResponse.noDeterminada(
                "PERFIL_ESTRUCTURADO",
                "La actividad se calcula directamente con los días y la duración declarados en el perfil",
                REGLA_VERSION_PERFIL);
    }
}
