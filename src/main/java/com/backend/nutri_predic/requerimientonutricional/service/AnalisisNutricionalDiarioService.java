package com.backend.nutri_predic.requerimientonutricional.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.alimentacion.nutricion.dto.NutrienteDiarioResponse;
import com.backend.nutri_predic.alimentacion.nutricion.service.ConsumoNutricionalDiarioService;
import com.backend.nutri_predic.plandia.entity.EstadoPlanDiario;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import com.backend.nutri_predic.requerimientonutricional.dto.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalisisNutricionalDiarioService {
    private final ClienteRepository clientes;
    private final ConsumoNutricionalDiarioService consumo;
    private final PlanDiarioRepository planes;
    public AnalisisNutricionalDiarioService(ClienteRepository clientes, ConsumoNutricionalDiarioService consumo, PlanDiarioRepository planes) {
        this.clientes = clientes; this.consumo = consumo; this.planes = planes;
    }
    @Transactional(readOnly = true)
    public AnalisisNutricionalDiarioResponse analizar(Long clienteId, LocalDate fecha) {
        var cliente = clientes.findById(clienteId).orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var plan = planes.findByClienteIdAndFechaObjetivo(clienteId, fecha).orElse(null);
        if (plan == null || plan.getEstado() != EstadoPlanDiario.DISPONIBLE) {
            var motivo = plan == null
                    ? "Todavía no se ha generado el plan nutricional para esta fecha"
                    : plan.getMotivoNoDisponible();
            return new AnalisisNutricionalDiarioResponse(clienteId, fecha, "NO_DISPONIBLE",
                    RequerimientoNutricionalResponse.noDisponible(motivo), null, null, null, null);
        }
        var objetivo = new RequerimientoNutricionalResponse(
                "DISPONIBLE", plan.getFuenteReferencia(), plan.getVersionReferencia(),
                plan.getEnergiaMinKcal(), plan.getProteinaMinG(), plan.getCarbohidratosMinG(),
                plan.getGrasasMinG(), "PLAN_DIARIO_PREDICTIVO", null);
        var real = consumo.obtener(clienteId, fecha);
        return new AnalisisNutricionalDiarioResponse(clienteId, fecha, "DISPONIBLE", objetivo,
                comparar(objetivo.kcalObjetivo(), real.kcal()), comparar(objetivo.proteinaObjetivoG(), real.proteinaG()),
                comparar(objetivo.carbohidratosObjetivoG(), real.carbohidratosG()), comparar(objetivo.grasasObjetivoG(), real.grasasG()));
    }
    private ComparacionNutrienteDiarioResponse comparar(BigDecimal objetivo, NutrienteDiarioResponse consumo) {
        if (objetivo == null || consumo == null || !consumo.calculable() || consumo.valor() == null)
            return new ComparacionNutrienteDiarioResponse(objetivo, null, null, null);
        return new ComparacionNutrienteDiarioResponse(objetivo, consumo.valor(), consumo.valor().subtract(objetivo),
                consumo.valor().multiply(BigDecimal.valueOf(100)).divide(objetivo, 2, RoundingMode.HALF_UP));
    }
}
