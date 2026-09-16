package com.backend.nutri_predic.prediccionmodelo.ciclo.service;

import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.exception.DatosV6IncompletosException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.prediccionmodelo.ciclo.dto.CicloDiarioResponse;
import com.backend.nutri_predic.prediccionmodelo.dto.*;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import com.backend.nutri_predic.prediccionmodelo.post.CicloPostPrediccionV5Service;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EstadoCicloDiario;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisLifecycleService;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import java.time.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CicloDiarioService {
    private static final ZoneId ZONA = ZoneId.of("America/Lima");
    private final AccessService access;
    private final PrediccionModeloRepository predicciones;
    private final EventoAnalisisRepository eventos;
    private final EventoAnalisisService analisis;
    private final CicloPostPrediccionV5Service post;
    private final ModeloPredictivoV6Service modelo;
    private final EventoAnalisisLifecycleService lifecycle;
    private final SesionConocimientoIaRepository sesiones;
    private final EvaluacionConsumoRepository evaluaciones;

    public CicloDiarioService(AccessService access, PrediccionModeloRepository predicciones,
            EventoAnalisisRepository eventos, EventoAnalisisService analisis, CicloPostPrediccionV5Service post,
            ModeloPredictivoV6Service modelo, EventoAnalisisLifecycleService lifecycle,
            SesionConocimientoIaRepository sesiones, EvaluacionConsumoRepository evaluaciones) {
        this.access = access; this.predicciones = predicciones; this.eventos = eventos; this.analisis = analisis;
        this.post = post; this.modelo = modelo; this.lifecycle = lifecycle;
        this.sesiones = sesiones; this.evaluaciones = evaluaciones;
    }

    /** Consulta el estado persistido del ciclo sin ejecutar ni reintentar ningún módulo. */
    public CicloDiarioResponse estado(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        LocalDate hoy = LocalDate.now(ZONA);
        var prediccion = predicciones
                .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                        clienteId, hoy, MomentoEvaluacion.DIARIO,
                        "variables-modelo-v6", EstadoPrediccionModelo.EXITOSA)
                .filter(p -> ModeloPredictivoV6Service.esModeloDiarioAdmitido(p.getModelVersion()))
                .orElse(null);
        if (prediccion == null) {
            var preparacion = modelo.preparacion(clienteId, hoy);
            return CicloDiarioResponse.pendiente(hoy, preparacion.datosFaltantes());
        }
        var evento = eventos.findFirstByPrediccionModeloIdAndOrigenResultadoOrderByIdAsc(
                prediccion.getId(), OrigenResultadoAnalisis.GENERADO).orElse(null);
        if (evento == null) {
            return CicloDiarioResponse.fallido(hoy, prediccion.getId(), true,
                    "La predicción existe, pero no tiene un evento original auditable.", null);
        }
        String pcc = sesiones.findByPrediccionModeloIdAndConfiguracionVersion(prediccion.getId(), "pcc-ia-v1")
                .map(s -> s.getEstado().name()).orElse("PENDIENTE");
        String pcs = evaluaciones.findFirstByPrediccionModeloIdOrderByFechaEvaluacionDesc(prediccion.getId())
                .map(e -> e.getEstadoClasificacion().name()).orElse("NO_DETERMINADA");
        var respuesta = AnalisisPredictivoResponse.from(evento, prediccion, pcc, pcs);
        if (evento.getEstadoCicloDiario() == EstadoCicloDiario.COMPLETADO) {
            return new CicloDiarioResponse("COMPLETADO", hoy, hoy.minusDays(1), true,
                    prediccion.getId(), "El ciclo diario está completo.", List.of(), respuesta);
        }
        if (evento.getEstadoCicloDiario() == EstadoCicloDiario.FALLIDO) {
            return CicloDiarioResponse.fallido(hoy, prediccion.getId(), true,
                    "El ciclo falló en el módulo " + evento.getModuloFalloCiclo() + ".", respuesta);
        }
        return new CicloDiarioResponse("PENDIENTE", hoy, hoy.minusDays(1), true,
                prediccion.getId(), "El ciclo diario todavía está pendiente.", List.of(), respuesta);
    }

    public synchronized CicloDiarioResponse asegurar(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        LocalDate hoy = LocalDate.now(ZONA);
        var existente = predicciones
                .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                        clienteId, hoy, MomentoEvaluacion.DIARIO,
                        "variables-modelo-v6", EstadoPrediccionModelo.EXITOSA)
                .filter(p -> ModeloPredictivoV6Service.esModeloDiarioAdmitido(p.getModelVersion()))
                .orElse(null);
        if (existente != null) {
            var evento = eventos.findFirstByPrediccionModeloIdAndOrigenResultadoOrderByIdAsc(
                    existente.getId(), OrigenResultadoAnalisis.GENERADO).orElse(null);
            if (evento == null) {
                return CicloDiarioResponse.fallido(hoy, existente.getId(), true,
                        "La predicción existe, pero no tiene un evento original auditable.", null);
            }
            if (evento.getEstadoCicloDiario() == EstadoCicloDiario.COMPLETADO) {
                var estadoGuardado = post.procesar(existente);
                var respuesta = AnalisisPredictivoResponse.from(
                        evento, existente, estadoGuardado.pccIa(), estadoGuardado.pcs());
                return new CicloDiarioResponse("COMPLETADO", hoy, hoy.minusDays(1), true,
                        existente.getId(), "El ciclo diario ya estaba disponible.", List.of(), respuesta);
            }
            long inicioActivo = System.nanoTime();
            var estadoPost = post.procesar(existente);
            long duracionMs = Math.max(0L, (System.nanoTime() - inicioActivo) / 1_000_000L);
            evento = lifecycle.registrarIntentoCiclo(evento.getId(), duracionMs, estadoPost.completo(),
                    estadoPost.moduloFallo(), estadoPost.motivoFallo());
            AnalisisPredictivoResponse respuesta = evento == null ? null
                    : AnalisisPredictivoResponse.from(evento, existente, estadoPost.pccIa(), estadoPost.pcs());
            if (!estadoPost.completo()) {
                return CicloDiarioResponse.fallido(hoy, existente.getId(), true,
                        "El ciclo no pudo completar el módulo " + estadoPost.moduloFallo() + ".", respuesta);
            }
            return new CicloDiarioResponse("COMPLETADO", hoy, hoy.minusDays(1), true,
                    existente.getId(), "El ciclo diario se completó reutilizando la predicción guardada.", List.of(), respuesta);
        }
        var preparacion = modelo.preparacion(clienteId, hoy);
        if (!preparacion.puedeAnalizar()) {
            return CicloDiarioResponse.pendiente(hoy, preparacion.datosFaltantes());
        }
        try {
            var respuesta = analisis.predecirInstrumentadoV6(
                    new AnalisisPredictivoRequest(clienteId, hoy, null, MomentoEvaluacion.DIARIO), auth);
            if (!"COMPLETADO".equals(respuesta.estadoCicloDiario())) {
                return CicloDiarioResponse.fallido(hoy, respuesta.prediccionId(), false,
                        "El ciclo no pudo completar el módulo " + respuesta.moduloFalloCiclo() + ".", respuesta);
            }
            return new CicloDiarioResponse("COMPLETADO", hoy, hoy.minusDays(1),
                    "REUTILIZADO".equals(respuesta.origenResultado()), respuesta.prediccionId(),
                    "Evaluación del día anterior completada.", List.of(), respuesta);
        } catch (DatosV6IncompletosException error) {
            return CicloDiarioResponse.pendiente(hoy, error.getDatosFaltantes());
        }
    }
}
