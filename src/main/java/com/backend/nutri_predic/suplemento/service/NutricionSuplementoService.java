package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.suplemento.dto.AporteSuplementoCalculado;
import com.backend.nutri_predic.suplemento.entity.*;
import com.backend.nutri_predic.suplemento.repository.ComponenteComposicionSuplementoRepository;
import java.math.*;
import org.springframework.stereotype.Service;

@Service
public class NutricionSuplementoService {
    private final ComponenteComposicionSuplementoRepository componentes;

    public NutricionSuplementoService(ComponenteComposicionSuplementoRepository c) {
        componentes = c;
    }

    public AporteSuplementoCalculado calcular(RegistroConsumoSuplemento r) {
        var propio = r == null ? null : r.getSuplementoCliente();
        if (propio != null && propio.getCantidadPorToma() != null && propio.getCantidadPorToma().signum() > 0
                && propio.getUnidadMedida() != null && r.getUnidad().getCodigo().equalsIgnoreCase(propio.getUnidadMedida().getCodigo())
                && propio.getProteinaGPorToma() != null && propio.getCarbohidratosGPorToma() != null && propio.getGrasasGPorToma() != null) {
            var f = r.getCantidadConsumida().divide(propio.getCantidadPorToma(), 12, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(r.getNumeroTomas() == null ? 1 : r.getNumeroTomas()));
            return new AporteSuplementoCalculado(true, propio.getProteinaGPorToma().multiply(f),
                    multiplicarOpcional(propio.getCreatinaGPorToma(), f),
                    multiplicarOpcional(propio.getCafeinaMgPorToma(), f),
                    propio.getCarbohidratosGPorToma().multiply(f), propio.getGrasasGPorToma().multiply(f),
                    multiplicarOpcional(propio.getSodioMgPorToma(), f));
        }
        var c = r == null ? null : r.getComposicionSuplemento();
        if (c == null
                || r.getCantidadConsumida() == null
                || c.getCantidadPorcionReferencia() == null
                || c.getCantidadPorcionReferencia().signum() <= 0)
            return AporteSuplementoCalculado.noCalculable();
        BigDecimal cantidad = r.getCantidadConsumida();
        if (!r.getUnidad().getCodigo().equalsIgnoreCase(c.getUnidadPorcion().getCodigo())) {
            var e = r.getEquivalenciaUnidad();
            if (e == null
                    || !e.getUnidadOrigen().getCodigo().equalsIgnoreCase(r.getUnidad().getCodigo())
                    || !e.getUnidadDestino()
                            .getCodigo()
                            .equalsIgnoreCase(c.getUnidadPorcion().getCodigo()))
                return AporteSuplementoCalculado.noCalculable();
            cantidad =
                    cantidad.multiply(e.getCantidadDestino())
                            .divide(e.getCantidadOrigen(), 12, RoundingMode.HALF_UP);
        }
        var f = cantidad.divide(c.getCantidadPorcionReferencia(), 12, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(r.getNumeroTomas() == null ? 1 : r.getNumeroTomas()));
        BigDecimal p = null, cr = null, ca = null, ch = null, g = null, s = null;
        for (var x : componentes.findByComposicionIdOrderByIdAsc(c.getId())) {
            var v = x.getCantidad().multiply(f);
            switch (x.getTipo()) {
                case PROTEINA -> p = suma(p, v);
                case CREATINA -> cr = suma(cr, v);
                case CAFEINA -> ca = suma(ca, v);
                case CARBOHIDRATOS -> ch = suma(ch, v);
                case GRASAS -> g = suma(g, v);
                case SODIO -> s = suma(s, v);
                default -> {}
            }
        }
        return new AporteSuplementoCalculado(true, p, cr, ca, ch, g, s);
    }

    /** Sólo devuelve energía declarada en la composición/etiqueta; nunca la infiere desde macros. */
    public BigDecimal calcularEnergiaKcal(RegistroConsumoSuplemento r) {
        var propio = r == null ? null : r.getSuplementoCliente();
        if (propio != null && propio.getCantidadPorToma() != null && propio.getCantidadPorToma().signum() > 0
                && propio.getUnidadMedida() != null && r.getUnidad().getCodigo().equalsIgnoreCase(propio.getUnidadMedida().getCodigo())
                && propio.getEnergiaKcalPorToma() != null)
            return propio.getEnergiaKcalPorToma().multiply(r.getCantidadConsumida())
                    .divide(propio.getCantidadPorToma(), 12, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(r.getNumeroTomas() == null ? 1 : r.getNumeroTomas()));
        var c = r == null ? null : r.getComposicionSuplemento();
        if (c == null || c.getEnergiaKcalPorcion() == null || r.getCantidadConsumida() == null
                || c.getCantidadPorcionReferencia() == null || c.getCantidadPorcionReferencia().signum() <= 0)
            return null;
        BigDecimal cantidad = r.getCantidadConsumida();
        if (!r.getUnidad().getCodigo().equalsIgnoreCase(c.getUnidadPorcion().getCodigo())) {
            var e = r.getEquivalenciaUnidad();
            if (e == null || !e.getUnidadOrigen().getCodigo().equalsIgnoreCase(r.getUnidad().getCodigo())
                    || !e.getUnidadDestino().getCodigo().equalsIgnoreCase(c.getUnidadPorcion().getCodigo())) return null;
            cantidad = cantidad.multiply(e.getCantidadDestino()).divide(e.getCantidadOrigen(), 12, RoundingMode.HALF_UP);
        }
        return c.getEnergiaKcalPorcion().multiply(cantidad)
                .divide(c.getCantidadPorcionReferencia(), 12, RoundingMode.HALF_UP);
    }

    private BigDecimal suma(BigDecimal a, BigDecimal b) {
        return a == null ? b : a.add(b);
    }

    private BigDecimal multiplicarOpcional(BigDecimal valor, BigDecimal factor) {
        return valor == null ? null : valor.multiply(factor);
    }
}
