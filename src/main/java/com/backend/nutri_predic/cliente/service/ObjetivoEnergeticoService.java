package com.backend.nutri_predic.cliente.service;

import com.backend.nutri_predic.cliente.entity.HistorialPerfilCliente;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Convierte objetivo físico del cliente en estrategia energética sin pedir DEFICIT/MANTENIMIENTO
 * directamente al usuario. Trazable por reglaVersion.
 */
@Service
public class ObjetivoEnergeticoService {
    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private static final String REGLA_VERSION = "OBJETIVO_FISICO_ENERGETICO_V1_2026";

    private final HistorialPerfilClienteRepository perfiles;

    public ObjetivoEnergeticoService(HistorialPerfilClienteRepository perfiles) {
        this.perfiles = perfiles;
    }

    public record Resolucion(ObjetivoEnergetico objetivoEnergetico, TipoObjetivoFisico tipoObjetivoFisico,
                             String fuente, String reglaVersion, String motivo) {}

    @Transactional(readOnly = true)
    public Resolucion resolver(Long clienteId, LocalDate fechaCorte) {
        var perfil = perfiles
                .findFirstByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(clienteId, fechaCorte)
                .filter(p -> !p.getCreadoEn().isAfter(fechaCorte.plusDays(1).atStartOfDay(ZONA).toInstant()))
                .orElse(null);
        if (perfil == null) return new Resolucion(null, null, "SIN_HISTORIAL", REGLA_VERSION, "No hay perfil histórico para fechaCorte");
        // Flujo nuevo: derivar desde objetivoFisico; objetivoEnergetico explícito solo compatibilidad/admin.
        var tipo = perfil.getTipoObjetivoFisico();
        if (tipo != null) {
            ObjetivoEnergetico derivado = switch (tipo) {
                case PERDER_PESO -> ObjetivoEnergetico.DEFICIT;
                case GANAR_PESO, GANAR_MASA_MUSCULAR -> ObjetivoEnergetico.SUPERAVIT;
                case MANTENER_PESO -> ObjetivoEnergetico.MANTENIMIENTO;
                case MEJORAR_RENDIMIENTO, RECOMPOSICION_CORPORAL, OTRO -> null;
            };
            if (derivado != null) {
                return new Resolucion(derivado, tipo, "DERIVADO_TIPO_OBJETIVO", REGLA_VERSION, null);
            }
            // OTRO/MEJORAR sin regla sustentada
            return new Resolucion(null, tipo, "NO_DETERMINADA_SIN_REGLA", REGLA_VERSION,
                    "TipoObjetivo " + tipo + " sin regla sustentada para estrategia energética");
        }
        // Sin tipo pero con explícito histórico: mantener compatibilidad
        if (perfil.getObjetivoEnergetico() != null) {
            return new Resolucion(perfil.getObjetivoEnergetico(), null,
                    "PERFIL_EXPLICITO_COMPAT", REGLA_VERSION, "Derivado de compatibilidad histórica; para flujo nuevo usar tipoObjetivoFisico");
        }
        return new Resolucion(null, null, "SIN_TIPO_OBJETIVO", REGLA_VERSION, "Falta tipoObjetivoFisico para derivar estrategia");
    }
}
