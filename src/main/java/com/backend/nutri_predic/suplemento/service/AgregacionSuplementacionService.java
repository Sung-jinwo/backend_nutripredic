package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.repository.RegistroConsumoSuplementoRepository;
import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
public class AgregacionSuplementacionService {
    private final RegistroConsumoSuplementoRepository consumos;
    private final NutricionSuplementoService nutricion;
    private final SuplementoClienteHistoricoService historico;

    public AgregacionSuplementacionService(
            RegistroConsumoSuplementoRepository c,
            NutricionSuplementoService n,
            SuplementoClienteHistoricoService h) {
        consumos = c;
        nutricion = n;
        historico = h;
    }

    @Transactional(readOnly = true)
    public ResumenSuplementacionResponse resumir(Long cliente, LocalDate corte) {
        var desde = corte.minusDays(6);
        var todos =
                consumos
                        .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                                cliente, desde, corte);
        var m =
                new HashMap<
                        LocalDate,
                        List<
                                com.backend.nutri_predic.suplemento.entity
                                        .RegistroConsumoSuplemento>>();
        for (var x : todos)
            m.computeIfAbsent(x.getRegistroHabito().getFecha(), k -> new ArrayList<>()).add(x);
        var dias = new ArrayList<ResumenSuplementacionDiario>();
        for (int i = 0; i < 7; i++)
            dias.add(dia(desde.plusDays(i), m.getOrDefault(desde.plusDays(i), List.of())));
        int total = todos.size(),
                calc =
                        dias.stream()
                                .mapToInt(ResumenSuplementacionDiario::registrosCalculables)
                                .sum(),
                diasCons = (int) dias.stream().filter(d -> d.registrosConsumoTotal() > 0).count(),
                diasCalc = (int) dias.stream().filter(d -> d.registrosCalculables() > 0).count();
        BigDecimal p = sum(dias, ResumenSuplementacionDiario::proteinaSuplementariaG),
                cr = sum(dias, ResumenSuplementacionDiario::creatinaG),
                ca = sum(dias, ResumenSuplementacionDiario::cafeinaMg),
                ch = sum(dias, ResumenSuplementacionDiario::carbohidratosSuplementariosG),
                g = sum(dias, ResumenSuplementacionDiario::grasasSuplementariasG),
                s = sum(dias, ResumenSuplementacionDiario::sodioMg);
        var hab = historico.resolver(cliente, corte);
        var v =
                new ResumenSuplementacionVentana(
                        desde,
                        corte,
                        7,
                        diasCons,
                        diasCalc,
                        total,
                        calc,
                        total - calc,
                        pct(calc, total),
                        p,
                        cr,
                        ca,
                        ch,
                        g,
                        s,
                        div(p, 7),
                        div(p, diasCalc));
        return new ResumenSuplementacionResponse(
                corte, List.copyOf(dias), v, hab, hab.size(), hab.size());
    }

    private ResumenSuplementacionDiario dia(
            LocalDate f,
            List<com.backend.nutri_predic.suplemento.entity.RegistroConsumoSuplemento> xs) {
        if (xs.isEmpty())
            return new ResumenSuplementacionDiario(
                    f, null, null, null, null, null, null, 0, 0, 0, null);
        BigDecimal p = null, cr = null, ca = null, ch = null, g = null, s = null;
        int calc = 0;
        for (var x : xs) {
            var a = nutricion.calcular(x);
            if (!a.calculable()) continue;
            calc++;
            p = add(p, a.proteinaG());
            cr = add(cr, a.creatinaG());
            ca = add(ca, a.cafeinaMg());
            ch = add(ch, a.carbohidratosG());
            g = add(g, a.grasasG());
            s = add(s, a.sodioMg());
        }
        return new ResumenSuplementacionDiario(
                f, p, cr, ca, ch, g, s, xs.size(), calc, xs.size() - calc, pct(calc, xs.size()));
    }

    private static BigDecimal add(BigDecimal a, BigDecimal b) {
        return b == null ? a : a == null ? b : a.add(b);
    }

    private static BigDecimal pct(int x, int n) {
        return n == 0
                ? null
                : BigDecimal.valueOf(x * 100L)
                        .divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal div(BigDecimal x, int n) {
        return x == null || n == 0
                ? null
                : x.divide(BigDecimal.valueOf(n), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal sum(
            List<ResumenSuplementacionDiario> d,
            java.util.function.Function<ResumenSuplementacionDiario, BigDecimal> f) {
        BigDecimal r = null;
        for (var x : d) r = add(r, f.apply(x));
        return r;
    }
}
