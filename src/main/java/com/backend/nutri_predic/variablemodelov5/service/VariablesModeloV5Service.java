package com.backend.nutri_predic.variablemodelov5.service;

import com.backend.nutri_predic.alimentacion.service.*;
import com.backend.nutri_predic.cliente.service.*;
import com.backend.nutri_predic.alimentacion.habito.repository.*;
import com.backend.nutri_predic.observaciondiaria.entity.*;
import com.backend.nutri_predic.observaciondiaria.service.*;
import com.backend.nutri_predic.suplemento.repository.*;
import com.backend.nutri_predic.suplemento.service.*;
import com.backend.nutri_predic.variablemodelov5.dto.*;
import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
public class VariablesModeloV5Service {
    private final PerfilClienteHistoricoService perfil;
    private final CompletitudVentanaV5Service completos;
    private final AgregacionNutricionalService alimentos;
    private final AgregacionSuplementacionService suplementos;
    private final RegistroHabitoRepository habitos;
    private final RegistroConsumoSuplementoRepository consumos;
    private final NutricionSuplementoService nutricion;

    public VariablesModeloV5Service(
            PerfilClienteHistoricoService p,
            CompletitudVentanaV5Service c,
            AgregacionNutricionalService a,
            AgregacionSuplementacionService s,
            RegistroHabitoRepository h,
            RegistroConsumoSuplementoRepository co,
            NutricionSuplementoService n) {
        perfil = p;
        completos = c;
        alimentos = a;
        suplementos = s;
        habitos = h;
        consumos = co;
        nutricion = n;
    }

    @Transactional(readOnly = true)
    public VariablesModeloV5Response construir(Long id, LocalDate corte) {
        var p = perfil.resolver(id, corte);
        boolean
                a =
                        completos
                                .verificar(id, corte, DominioObservacionDiaria.ALIMENTACION)
                                .ventanaCompleta(),
                s =
                        completos
                                .verificar(id, corte, DominioObservacionDiaria.SUPLEMENTACION)
                                .ventanaCompleta(),
                h =
                        completos
                                .verificar(id, corte, DominioObservacionDiaria.HABITOS)
                                .ventanaCompleta();
        var n = a ? alimentos.resumir(id, corte).ventana() : null;
        var hs =
                habitos.findByClienteIdAndFechaBetweenOrderByFechaAscIdAsc(
                        id, corte.minusDays(6), corte);
        var cs =
                consumos
                        .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                                id, corte.minusDays(6), corte);
        var f =
                new VariablesModeloV5Response.Features(
                        p.edad(),
                        p.pesoKg(),
                        p.alturaCm(),
                        p.tipoObjetivoFisico() == null ? null : p.tipoObjetivoFisico().name(),
                        nut(n, x -> x.kcal()),
                        nut(n, x -> x.proteinaG()),
                        nut(n, x -> x.carbohidratosG()),
                        nut(n, x -> x.grasasG()),
                        nut(n, x -> x.fibraG()),
                        nut(n, x -> x.azucarG()),
                        nut(n, x -> x.sodioMg()),
                        s ? comp(cs, x -> x.proteinaG()) : null,
                        s ? comp(cs, x -> x.creatinaG()) : null,
                        s ? comp(cs, x -> x.cafeinaMg()) : null,
                        s ? comp(cs, x -> x.carbohidratosG()) : null,
                        s ? comp(cs, x -> x.grasasG()) : null,
                        h ? avg(hs, x -> x.getCantidadComidas()) : null,
                        h ? avg(hs, x -> x.getConsumoAgua()) : null,
                        h ? prop(hs, x -> x.getDesayuno()) : null,
                        h ? prop(hs, x -> x.getSnacks()) : null,
                        h ? avg(hs, x -> x.getComidasCocinadas()) : null);
        return new VariablesModeloV5Response(
                "variables-modelo-v5",
                corte,
                f,
                new VariablesModeloV5Response.Metadata(id, null, p.fuentePerfil(), a, s, h));
    }

    private BigDecimal nut(
            com.backend.nutri_predic.alimentacion.dto.ResumenNutricionalVentana n,
            Function<
                            com.backend.nutri_predic.alimentacion.dto.ResumenNutricionalVentana,
                            com.backend.nutri_predic.alimentacion.dto.ResumenNutrienteVentana>
                    f) {
        if (n == null) return null;
        var x = f.apply(n);
        return Boolean.TRUE.equals(x.completo()) ? x.promedioSobreVentanaConocido() : null;
    }

    private BigDecimal comp(
            List<com.backend.nutri_predic.suplemento.entity.RegistroConsumoSuplemento> xs,
            Function<com.backend.nutri_predic.suplemento.dto.AporteSuplementoCalculado, BigDecimal>
                    f) {
        BigDecimal r = BigDecimal.ZERO;
        for (var x : xs) {
            var a = nutricion.calcular(x);
            var v = f.apply(a);
            if (!a.calculable() || v == null) return null;
            r = r.add(v);
        }
        return r.divide(BigDecimal.valueOf(7), 6, RoundingMode.HALF_UP);
    }

    private <T> BigDecimal avg(List<T> x, Function<T, ? extends Number> f) {
        if (x.size() != 7) return null;
        BigDecimal r = BigDecimal.ZERO;
        for (var z : x) {
            var v = f.apply(z);
            if (v == null) return null;
            r = r.add(BigDecimal.valueOf(v.doubleValue()));
        }
        return r.divide(BigDecimal.valueOf(7), 6, RoundingMode.HALF_UP);
    }

    private <T> BigDecimal prop(List<T> x, Function<T, Boolean> f) {
        if (x.size() != 7) return null;
        long n = 0;
        for (var z : x) {
            var v = f.apply(z);
            if (v == null) return null;
            if (v) n++;
        }
        return BigDecimal.valueOf(n).divide(BigDecimal.valueOf(7), 6, RoundingMode.HALF_UP);
    }
}
