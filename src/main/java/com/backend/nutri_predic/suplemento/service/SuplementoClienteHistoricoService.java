package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.entity.*;
import com.backend.nutri_predic.suplemento.repository.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
public class SuplementoClienteHistoricoService {
    private final SuplementoClienteRepository actuales;
    private final HistorialSuplementoClienteRepository historial;

    public SuplementoClienteHistoricoService(
            SuplementoClienteRepository a, HistorialSuplementoClienteRepository h) {
        actuales = a;
        historial = h;
    }

    @Transactional(readOnly = true)
    public List<SuplementoClienteHistoricoResult> resolver(Long clienteId, LocalDate corte) {
        return actuales.findByClienteIdOrderBySuplementoNombreAscIdAsc(clienteId).stream()
                .map(a -> version(a, corte))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<SuplementoClienteHistoricoResult> version(
            SuplementoCliente a, LocalDate corte) {
        var limite = corte.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        var h =
                historial.findByAsignacionIdOrderByRegistradoEnAscIdAsc(a.getId()).stream()
                        .filter(x -> !x.getRegistradoEn().isAfter(limite))
                        .reduce((x, y) -> y);
        SuplementoClienteHistoricoResult r;
        if (h.isPresent()) {
            var x = h.get();
            r =
                    new SuplementoClienteHistoricoResult(
                            a.getId(),
                            a.getSuplemento().getId(),
                            x.getCantidadPorToma(),
                            x.getUnidadMedida() == null ? null : x.getUnidadMedida().getCodigo(),
                            x.getTomasPorPeriodo(),
                            x.getPeriodoFrecuencia() == null
                                    ? null
                                    : x.getPeriodoFrecuencia().name(),
                            x.getFechaInicio(),
                            x.getFechaFin(),
                            x.getActivo(),
                            "HISTORIAL",
                            x.getRegistradoEn());
        } else
            r =
                    new SuplementoClienteHistoricoResult(
                            a.getId(),
                            a.getSuplemento().getId(),
                            a.getCantidadPorToma(),
                            a.getUnidadMedida() == null ? null : a.getUnidadMedida().getCodigo(),
                            a.getTomasPorPeriodo(),
                            a.getPeriodoFrecuencia() == null
                                    ? null
                                    : a.getPeriodoFrecuencia().name(),
                            a.getFechaInicio(),
                            a.getFechaFin(),
                            a.getActivo(),
                            "FALLBACK_ACTUAL",
                            null);
        return aplicable(r, corte) ? Optional.of(r) : Optional.empty();
    }

    private boolean aplicable(SuplementoClienteHistoricoResult r, LocalDate c) {
        return Boolean.TRUE.equals(r.activo())
                && r.fechaInicio() != null
                && !r.fechaInicio().isAfter(c)
                && (r.fechaFin() == null || !r.fechaFin().isBefore(c));
    }
}
