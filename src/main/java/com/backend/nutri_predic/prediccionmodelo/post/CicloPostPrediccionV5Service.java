package com.backend.nutri_predic.prediccionmodelo.post;

import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.conocimiento.practica.service.GeneracionPreguntasConocimientoService;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoRequest;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.service.ConsumoEvaluacionExtractor;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.plandia.service.IntervencionConsumoDiariaService;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class CicloPostPrediccionV5Service {
    private static final String CONFIG_PCC = "pcc-ia-v1";
    private final GeneracionPreguntasConocimientoService pcc;
    private final SesionConocimientoIaRepository sesiones;
    private final ConsumoEvaluacionExtractor pcs;
    private final EvaluacionConsumoRepository evaluaciones;
    private final RegistroFalloCicloPostPrediccionService fallos;
    private final PlanDiarioService planes;
    private final IntervencionConsumoDiariaService intervenciones;
    public CicloPostPrediccionV5Service(GeneracionPreguntasConocimientoService pcc, SesionConocimientoIaRepository sesiones, ConsumoEvaluacionExtractor pcs, EvaluacionConsumoRepository evaluaciones, RegistroFalloCicloPostPrediccionService fallos, PlanDiarioService planes, IntervencionConsumoDiariaService intervenciones) {
        this.pcc = pcc; this.sesiones = sesiones; this.pcs = pcs; this.evaluaciones = evaluaciones; this.fallos = fallos; this.planes=planes; this.intervenciones=intervenciones;
    }
    public EstadoCicloPostPrediccion procesar(PrediccionModelo prediccion) {
        var plan = procesarPlan(prediccion);
        String estadoPcc = procesarPcc(prediccion);
        String estadoPcs = procesarPcs(prediccion);
        procesarIntervencion(prediccion, plan);
        return new EstadoCicloPostPrediccion(estadoPcc, estadoPcs);
    }

    private com.backend.nutri_predic.plandia.entity.PlanDiario procesarPlan(PrediccionModelo p) {
        try {
            return planes.generarOReutilizar(p);
        } catch (RuntimeException error) { registrarSeguro(p.getId(), "PLAN_DIARIO", error); return null; }
    }
    private void procesarIntervencion(PrediccionModelo p, com.backend.nutri_predic.plandia.entity.PlanDiario plan) {
        if (plan == null) return;
        try {
            evaluaciones.findFirstByPrediccionModeloIdOrderByFechaEvaluacionDesc(p.getId())
                    .ifPresent(e -> intervenciones.crearPorExcesos(e, plan));
        } catch (RuntimeException error) { registrarSeguro(p.getId(), "INTERVENCION", error); }
    }
    private String procesarPcc(PrediccionModelo p) {
        try {
            var existente = sesiones.findByPrediccionModeloIdAndConfiguracionVersion(p.getId(), CONFIG_PCC);
            var sesion = existente.isPresent() ? existente.get() : null;
            if (sesion == null) {
                var generada = pcc.generarAutomatico(p.getId());
                return "IA_NO_DISPONIBLE".equals(generada.estadoAdaptativo()) ? "NO_DISPONIBLE" : "GENERADA";
            }
            var estado = sesion.getEstado();
            return estado == EstadoSesionConocimientoIa.IA_NO_DISPONIBLE ? "NO_DISPONIBLE" : "GENERADA";
        } catch (RuntimeException error) {
            registrarSeguro(p.getId(), "PCC_IA", error);
            return "NO_DISPONIBLE";
        }
    }
    private String procesarPcs(PrediccionModelo p) {
        LocalDate inicio = p.getFechaCorte();
        try {
            var existente = evaluaciones.findFirstByClienteIdAndFechaCorteAndFechaInicioAndVentanaDiasAndSchemaVersionOrderByFechaEvaluacionDesc(
                    p.getCliente().getId(), p.getFechaCorte(), inicio, 1, "pcs-factual-v1");
            if (existente.isPresent()) {
                pcs.vincularPrediccionAutomatica(existente.get().getId(), p.getId());
                return existente.get().getEstadoClasificacion().name();
            }
            var respuesta = pcs.extraerAutomatico(new EvaluacionConsumoRequest(
                    p.getCliente().getId(), p.getFechaCorte(), 1, p.getMomentoEvaluacion()));
            pcs.vincularPrediccionAutomatica(respuesta.id(), p.getId());
            return respuesta.estadoClasificacion();
        } catch (RuntimeException error) {
            registrarSeguro(p.getId(), "PCS", error);
            return "NO_DETERMINADA";
        }
    }
    private void registrarSeguro(Long prediccionId, String modulo, RuntimeException error) {
        try { fallos.registrar(prediccionId, modulo, error); } catch (RuntimeException ignored) { }
    }
}
