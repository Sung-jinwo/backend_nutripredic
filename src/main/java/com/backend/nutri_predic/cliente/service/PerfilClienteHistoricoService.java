package com.backend.nutri_predic.cliente.service;

import com.backend.nutri_predic.cliente.dto.PerfilClienteHistoricoResponse;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilClienteHistoricoService {
    private static final ZoneId ZONA_CORTE = ZoneId.of("America/Lima");
    private final ClienteRepository clientes;
    private final HistorialPerfilClienteRepository historial;

    public PerfilClienteHistoricoService(
            ClienteRepository clientes, HistorialPerfilClienteRepository historial) {
        this.clientes = clientes;
        this.historial = historial;
    }

    @Transactional(readOnly = true)
    public PerfilClienteHistoricoResponse resolver(Long clienteId, LocalDate fechaCorte) {
        if (fechaCorte == null) throw new IllegalArgumentException("fechaCorte es obligatoria");
        var cliente =
                clientes.findById(clienteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var limite = fechaCorte.plusDays(1).atStartOfDay(ZONA_CORTE).toInstant();
        var version =
                historial
                        .findByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(
                                clienteId, fechaCorte)
                        .stream()
                        .filter(perfil -> !perfil.getCreadoEn().isAfter(limite))
                        .findFirst()
                        .orElse(null);
        if (version == null) {
            return new PerfilClienteHistoricoResponse(
                    clienteId,
                    fechaCorte,
                    cliente.getEdad(),
                    cliente.getSexo(),
                    cliente.getPesoKg(),
                    cliente.getAlturaCm(),
                    cliente.getImc(),
                    cliente.getTipoObjetivoFisico(),
                    cliente.getObjetivoFisico(),
                    cliente.getRealizaActividadFisica(),
                    cliente.getDiasEntrenamientoSemana(),
                    cliente.getTipoActividadFisica(),
                    cliente.getTipoEntrenamiento(),
                    cliente.getDuracionPromedioSesionMinutos(),
                    cliente.getObjetivoEnergetico(),
                    "FALLBACK_ACTUAL",
                    null);
        }
        return new PerfilClienteHistoricoResponse(
                clienteId,
                fechaCorte,
                version.getEdad(),
                version.getSexo(),
                version.getPesoKg(),
                version.getAlturaCm(),
                version.getImc(),
                version.getTipoObjetivoFisico(),
                version.getObjetivoFisico(),
                version.getRealizaActividadFisica(),
                version.getDiasEntrenamientoSemana(),
                version.getTipoActividadFisica(),
                version.getTipoEntrenamiento(),
                version.getDuracionPromedioSesionMinutos(),
                version.getObjetivoEnergetico(),
                "HISTORIAL",
                version.getCreadoEn());
    }
}
