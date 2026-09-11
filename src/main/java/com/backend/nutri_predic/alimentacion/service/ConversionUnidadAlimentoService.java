package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.*;
import java.math.*;
import org.springframework.stereotype.Service;

@Service
public class ConversionUnidadAlimentoService {
    public ConversionUnidadAlimentoResultado convertir(RegistroAlimento r, String d) {
        if (r == null || r.getUnidad() == null || r.getCantidad() == null || d == null)
            return ConversionUnidadAlimentoResultado.noCalculable();
        if (r.getUnidad().getCodigo().equalsIgnoreCase(d))
            return new ConversionUnidadAlimentoResultado(true, r.getCantidad(), d, null);
        var e = r.getEquivalenciaUnidad();
        if (e == null
                || !e.getAlimento().getId().equals(r.getAlimento().getId())
                || !e.getUnidadOrigen().getCodigo().equalsIgnoreCase(r.getUnidad().getCodigo())
                || !e.getUnidadDestino().getCodigo().equalsIgnoreCase(d))
            return ConversionUnidadAlimentoResultado.noCalculable();
        return new ConversionUnidadAlimentoResultado(
                true,
                r.getCantidad()
                        .multiply(e.getCantidadDestino())
                        .divide(e.getCantidadOrigen(), 12, RoundingMode.HALF_UP),
                d,
                e.getId());
    }
}
