package com.backend.nutri_predic.requerimientonutricional.service;

import com.backend.nutri_predic.actividadfisica.dto.NivelActividadFisicaResponse;
import com.backend.nutri_predic.actividadfisica.service.NivelActividadFisicaService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.service.ObjetivoEnergeticoService;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.plandia.entity.EstadoPlanDiario;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import com.backend.nutri_predic.requerimientonutricional.dto.ObjetivoNutricionalResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObjetivoNutricionalService {
    private final ClienteRepository clientes;
    private final NivelActividadFisicaService nivelActividad;
    private final ObjetivoEnergeticoService objetivoEnergetico;
    private final PlanDiarioRepository planes;

    public ObjetivoNutricionalService(ClienteRepository clientes, NivelActividadFisicaService nivelActividad,
            ObjetivoEnergeticoService objetivoEnergetico, PlanDiarioRepository planes) {
        this.clientes = clientes;
        this.nivelActividad = nivelActividad;
        this.objetivoEnergetico = objetivoEnergetico;
        this.planes = planes;
    }

    @Transactional(readOnly = true)
    public ObjetivoNutricionalResponse resolver(Long clienteId, LocalDate fecha) {
        var cliente = clientes.findById(clienteId).orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var actividad = nivelActividad.resolver(clienteId, fecha);
        var energetico = objetivoEnergetico.resolver(clienteId, fecha);

        List<String> pendientes = new ArrayList<>();
        if (cliente.getPesoKg() == null) pendientes.add("PESO");
        if (cliente.getAlturaCm() == null) pendientes.add("ALTURA");
        if (cliente.getEdad() == null) pendientes.add("EDAD");
        if (cliente.getSexo() == null) pendientes.add("SEXO");
        if (energetico.objetivoEnergetico() == null) pendientes.add("OBJETIVO_ENERGETICO");

        var plan = planes.findByClienteIdAndFechaObjetivo(clienteId, fecha).orElse(null);
        if (plan == null || plan.getEstado() != EstadoPlanDiario.DISPONIBLE) {
            String motivo = plan == null
                    ? "Todavía no se ha generado el plan nutricional para esta fecha"
                    : plan.getMotivoNoDisponible();
            if (!pendientes.isEmpty()) motivo = motivo + " | Pendientes: " + String.join(",", pendientes);
            return ObjetivoNutricionalResponse.noDisponible(motivo, pendientes,
                    energetico.tipoObjetivoFisico(), energetico.objetivoEnergetico(), actividad);
        }
        return new ObjetivoNutricionalResponse("DISPONIBLE",
                energetico.tipoObjetivoFisico(), energetico.objetivoEnergetico(),
                actividad,
                plan.getEnergiaMinKcal(), plan.getProteinaMinG(), plan.getCarbohidratosMinG(), plan.getGrasasMinG(),
                plan.getFuenteReferencia(), plan.getVersionReferencia(), null, pendientes.isEmpty() ? null : pendientes);
    }
}
