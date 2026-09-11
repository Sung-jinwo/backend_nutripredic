package com.backend.nutri_predic.alimentacion.nutricion.service;

import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.alimentacion.service.NutricionAlimentoService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.alimentacion.nutricion.dto.ResumenNutricionalDiarioResponse;
import com.backend.nutri_predic.plandia.entity.EstadoPlanDiario;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import com.backend.nutri_predic.suplemento.repository.RegistroConsumoSuplementoRepository;
import com.backend.nutri_predic.suplemento.service.NutricionSuplementoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResumenNutricionalDiarioService {
    private final ClienteRepository clientes;
    private final RegistroHabitoRepository habitos;
    private final RegistroAlimentoRepository alimentos;
    private final RegistroConsumoSuplementoRepository suplementos;
    private final NutricionAlimentoService nutricionAlimento;
    private final NutricionSuplementoService nutricionSuplemento;
    private final PlanDiarioRepository planes;

    public ResumenNutricionalDiarioService(ClienteRepository clientes, RegistroHabitoRepository habitos,
            RegistroAlimentoRepository alimentos, RegistroConsumoSuplementoRepository suplementos,
            NutricionAlimentoService nutricionAlimento, NutricionSuplementoService nutricionSuplemento,
            PlanDiarioRepository planes) {
        this.clientes = clientes; this.habitos = habitos; this.alimentos = alimentos; this.suplementos = suplementos;
        this.nutricionAlimento = nutricionAlimento; this.nutricionSuplemento = nutricionSuplemento; this.planes = planes;
    }

    @Transactional(readOnly = true)
    public ResumenNutricionalDiarioResponse resumen(Long clienteId, LocalDate fecha) {
        var cliente = clientes.findById(clienteId).orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var habitoOpt = habitos.findByClienteIdAndFecha(clienteId, fecha);
        var registrosAlimento = alimentos.findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(clienteId, fecha, fecha);
        var registrosSuplemento = suplementos.findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(clienteId, fecha, fecha);

        // Alimentos totales
        BigDecimal aKcal = BigDecimal.ZERO, aProt = BigDecimal.ZERO, aCho = BigDecimal.ZERO, aGrasa = BigDecimal.ZERO,
                aFibra = BigDecimal.ZERO, aAzucar = BigDecimal.ZERO, aSodio = BigDecimal.ZERO;
        boolean aCalc = !registrosAlimento.isEmpty();
        int aCalcCount = 0;
        for (var r : registrosAlimento) {
            var ap = nutricionAlimento.calcular(r);
            if (!ap.calculable()) { aCalc = false; continue; }
            aCalcCount++;
            if (ap.kcal() != null) aKcal = aKcal.add(ap.kcal());
            if (ap.proteinaG() != null) aProt = aProt.add(ap.proteinaG());
            if (ap.carbohidratosG() != null) aCho = aCho.add(ap.carbohidratosG());
            if (ap.grasasG() != null) aGrasa = aGrasa.add(ap.grasasG());
            if (ap.fibraG() != null) aFibra = aFibra.add(ap.fibraG());
            if (ap.azucarG() != null) aAzucar = aAzucar.add(ap.azucarG());
            if (ap.sodioMg() != null) aSodio = aSodio.add(ap.sodioMg());
        }
        boolean alimentosCalculable = aCalc && aCalcCount == registrosAlimento.size();

        // Suplementos totales
        BigDecimal sKcal = BigDecimal.ZERO, sProt = BigDecimal.ZERO, sCho = BigDecimal.ZERO, sGrasa = BigDecimal.ZERO,
                sCreatina = BigDecimal.ZERO, sCafeina = BigDecimal.ZERO;
        boolean sCalc = true;
        int sCalcCount = 0;
        for (var r : registrosSuplemento) {
            var ap = nutricionSuplemento.calcular(r);
            var e = nutricionSuplemento.calcularEnergiaKcal(r);
            if (e != null) sKcal = sKcal.add(e);
            // si no hay energia declarada, no marca noCalculable — suplementos pueden no tener kcal
            if (!ap.calculable() && (r.getComposicionSuplemento() != null)) { sCalc = false; }
            else if (ap.calculable()) {
                sCalcCount++;
                if (ap.proteinaG() != null) sProt = sProt.add(ap.proteinaG());
                if (ap.carbohidratosG() != null) sCho = sCho.add(ap.carbohidratosG());
                if (ap.grasasG() != null) sGrasa = sGrasa.add(ap.grasasG());
                if (ap.creatinaG() != null) sCreatina = sCreatina.add(ap.creatinaG());
                if (ap.cafeinaMg() != null) sCafeina = sCafeina.add(ap.cafeinaMg());
            }
        }
        boolean supCalculable = registrosSuplemento.isEmpty() || (sCalc && sCalcCount == registrosSuplemento.size() || sKcal.signum() != 0);
        // Si no hay suplementos, marcar calculable true con ceros para simplicidad UI

        BigDecimal tKcal = aKcal.add(sKcal);
        BigDecimal tProt = aProt.add(sProt);
        BigDecimal tCho = aCho.add(sCho);
        BigDecimal tGrasa = aGrasa.add(sGrasa);

        var plan = planes.findByClienteIdAndFechaObjetivo(clienteId, fecha).orElse(null);
        boolean planPredictivoDisponible = plan != null
                && plan.getEstado() == EstadoPlanDiario.DISPONIBLE;
        String estadoObjetivo = planPredictivoDisponible ? "DISPONIBLE" : "PENDIENTE_MODELO";
        BigDecimal objetivoKcal = planPredictivoDisponible ? plan.getEnergiaMaxKcal() : null;
        BigDecimal objetivoProteina = planPredictivoDisponible ? plan.getProteinaMaxG() : null;
        BigDecimal objetivoCarbohidratos = planPredictivoDisponible ? plan.getCarbohidratosMaxG() : null;
        BigDecimal objetivoGrasas = planPredictivoDisponible ? plan.getGrasasMaxG() : null;
        String motivoObjetivo = plan == null
                ? "Aún no existe un plan diario generado por el análisis predictivo para esta fecha"
                : plan.getMotivoNoDisponible();
        ResumenNutricionalDiarioResponse.ObjetivoResumen objetivo = new ResumenNutricionalDiarioResponse.ObjetivoResumen(
                estadoObjetivo, objetivoKcal, objetivoProteina, objetivoCarbohidratos, objetivoGrasas,
                planPredictivoDisponible ? plan.getFuenteReferencia() : null,
                planPredictivoDisponible ? plan.getVersionReferencia() : null, motivoObjetivo);

        var alimentosResumen = new ResumenNutricionalDiarioResponse.AlimentosNutrientes(
                nullIfZeroAlimentos(aKcal, alimentosCalculable), nullIfZeroAlimentos(aProt, alimentosCalculable),
                nullIfZeroAlimentos(aCho, alimentosCalculable), nullIfZeroAlimentos(aGrasa, alimentosCalculable),
                nullIfZeroAlimentos(aFibra, alimentosCalculable), nullIfZeroAlimentos(aAzucar, alimentosCalculable),
                nullIfZeroAlimentos(aSodio, alimentosCalculable), alimentosCalculable);

        var supResumen = new ResumenNutricionalDiarioResponse.SuplementosNutrientes(
                registrosSuplemento.isEmpty() ? BigDecimal.ZERO : sKcal,
                nullIfZeroSup(sProt, registrosSuplemento), nullIfZeroSup(sCho, registrosSuplemento),
                nullIfZeroSup(sGrasa, registrosSuplemento), nullIfZeroSup(sCreatina, registrosSuplemento),
                nullIfZeroSup(sCafeina, registrosSuplemento), supCalculable);

        boolean totalCalculable = alimentosCalculable;
        var totalResumen = new ResumenNutricionalDiarioResponse.TotalNutrientes(
                tKcal, tProt, tCho, tGrasa, aFibra, aAzucar, aSodio, totalCalculable);

        var consumido = new ResumenNutricionalDiarioResponse.ConsumidoResumen(
                alimentosResumen, supResumen, totalResumen, registrosAlimento.size(), registrosSuplemento.size());

        ResumenNutricionalDiarioResponse.DiferenciaResumen diferencia = null;
        ResumenNutricionalDiarioResponse.PorcentajeResumen porcentaje = null;
        if ("DISPONIBLE".equals(estadoObjetivo) && totalCalculable) {
            diferencia = new ResumenNutricionalDiarioResponse.DiferenciaResumen(
                    tKcal.subtract(objetivoKcal), tProt.subtract(objetivoProteina),
                    tCho.subtract(objetivoCarbohidratos), tGrasa.subtract(objetivoGrasas));
            porcentaje = new ResumenNutricionalDiarioResponse.PorcentajeResumen(
                    pct(tKcal, objetivoKcal), pct(tProt, objetivoProteina),
                    pct(tCho, objetivoCarbohidratos), pct(tGrasa, objetivoGrasas));
        }

        // Agua: RegistroHabito.consumoAgua es litros (Double). Convertir a ml inequívoco.
        Double litros = habitoOpt.map(h -> h.getConsumoAgua()).orElse(null);
        Integer ml = litros == null ? null : (int) Math.round(litros * 1000);
        Boolean consumeSup = habitoOpt.map(h -> h.getConsumeSuplementos()).orElse(null);
        boolean declarado = litros != null;
        BigDecimal objetivoAguaMl = planPredictivoDisponible ? plan.getAguaMaxMl() : null;
        var agua = new ResumenNutricionalDiarioResponse.AguaResumen(
                ml, objetivoAguaMl, litros, declarado, consumeSup);

        String estado = "DISPONIBLE".equals(estadoObjetivo) ? "DISPONIBLE" : "OBJETIVO_NO_DISPONIBLE_CONSUMIDO_VISIBLE";
        String motivo = "DISPONIBLE".equals(estadoObjetivo) ? null : motivoObjetivo;

        return new ResumenNutricionalDiarioResponse(fecha, objetivo, consumido, diferencia, porcentaje, agua, estado, motivo);
    }

    private BigDecimal pct(BigDecimal consumido, BigDecimal objetivo) {
        if (objetivo == null || objetivo.signum() == 0) return null;
        return consumido.multiply(BigDecimal.valueOf(100)).divide(objetivo, 2, RoundingMode.HALF_UP);
    }
    private BigDecimal nullIfZeroAlimentos(BigDecimal v, boolean calc) { return calc ? v : null; }
    private BigDecimal nullIfZeroSup(BigDecimal v, java.util.List<?> regs) { return v == null && regs != null && regs.isEmpty() ? BigDecimal.ZERO : v; }
    private BigDecimal nullIfZeroSup(BigDecimal v, int count) { return v; }
}
