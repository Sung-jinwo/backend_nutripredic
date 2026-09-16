package com.backend.nutri_predic.prediccionmodelo.evento.service;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.estudio.entity.EstadoEstudio;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import com.backend.nutri_predic.estudio.repository.ParticipacionEstudioRepository;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.prediccionmodelo.evento.dto.EventoAnalisisResponse;
import com.backend.nutri_predic.prediccionmodelo.evento.dto.FinalizarAnalisisRequest;
import com.backend.nutri_predic.prediccionmodelo.evento.dto.InicioAnalisisRequest;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.EventoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.ProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.TipoProcedimientoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.EventoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.evento.repository.ProcedimientoAnalisisRepository;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoRequest;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoResponse;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoService;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service;
import com.backend.nutri_predic.prediccionmodelo.service.PrediccionModeloFallidaException;
import com.backend.nutri_predic.prediccionmodelo.service.ResultadoInferenciaModelo;
import com.backend.nutri_predic.prediccionmodelo.service.TrazabilidadInferencia;
import com.backend.nutri_predic.prediccionmodelo.post.CicloPostPrediccionV5Service;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventoAnalisisService {
    private static final String CODIGO_SOFTWARE_IA = "SOFTWARE_IA";

    private final EventoAnalisisRepository eventos;
    private final ProcedimientoAnalisisRepository procedimientos;
    private final ParticipacionEstudioRepository participaciones;
    private final UsuarioRepository usuarios;
    private final AccessService access;
    private final ModeloPredictivoService modeloPredictivo;
    private final ModeloPredictivoV6Service modeloPredictivoV6;
    private final EventoAnalisisLifecycleService lifecycle;
    private final CicloPostPrediccionV5Service cicloPostPrediccion;

    @Autowired
    public EventoAnalisisService(
            EventoAnalisisRepository eventos,
            ProcedimientoAnalisisRepository procedimientos,
            ParticipacionEstudioRepository participaciones,
            UsuarioRepository usuarios,
            AccessService access,
            ModeloPredictivoService modeloPredictivo,
            ModeloPredictivoV6Service modeloPredictivoV6,
            EventoAnalisisLifecycleService lifecycle,
            CicloPostPrediccionV5Service cicloPostPrediccion) {
        this.eventos = eventos;
        this.procedimientos = procedimientos;
        this.participaciones = participaciones;
        this.usuarios = usuarios;
        this.access = access;
        this.modeloPredictivo = modeloPredictivo;
        this.modeloPredictivoV6 = modeloPredictivoV6;
        this.lifecycle = lifecycle;
        this.cicloPostPrediccion = cicloPostPrediccion;
    }

    /** Compatibilidad para construcción interna previa al ciclo post-predicción. */
    public EventoAnalisisService(
            EventoAnalisisRepository eventos,
            ProcedimientoAnalisisRepository procedimientos,
            ParticipacionEstudioRepository participaciones,
            UsuarioRepository usuarios,
            AccessService access,
            ModeloPredictivoService modeloPredictivo,
            ModeloPredictivoV6Service modeloPredictivoV6,
            EventoAnalisisLifecycleService lifecycle) {
        this(eventos, procedimientos, participaciones, usuarios, access, modeloPredictivo, modeloPredictivoV6, lifecycle, null);
    }

    /** Compatibilidad para pruebas históricas del flujo V5. */
    public EventoAnalisisService(
            EventoAnalisisRepository eventos,
            ProcedimientoAnalisisRepository procedimientos,
            ParticipacionEstudioRepository participaciones,
            UsuarioRepository usuarios,
            AccessService access,
            ModeloPredictivoService modeloPredictivo,
            EventoAnalisisLifecycleService lifecycle) {
        this(eventos, procedimientos, participaciones, usuarios, access, modeloPredictivo, null, lifecycle, null);
    }

    public EventoAnalisisResponse iniciar(
            InicioAnalisisRequest request, Authentication authentication) {
        ProcedimientoAnalisis procedimiento =
                procedimientos
                        .findById(request.procedimientoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Procedimiento"));
        Contexto contexto =
                validarContexto(
                        request.clienteId(),
                        procedimiento,
                        request.participacionEstudioId(),
                        authentication);

        EventoAnalisis evento =
                lifecycle.crear(
                        contexto.cliente(),
                        procedimiento,
                        request.fechaCorte(),
                        request.momento(),
                        contexto.participacion(),
                        contexto.evaluador());
        if (procedimiento.getTipo() == TipoProcedimientoAnalisis.MANUAL) {
            return EventoAnalisisResponse.from(evento);
        }

        EjecucionSoftwareIa ejecucion = ejecutarSoftwareIa(evento, false);
        return EventoAnalisisResponse.from(ejecucion.evento());
    }

public AnalisisPredictivoResponse predecirInstrumentado(
            AnalisisPredictivoRequest request, Authentication authentication) {
        ProcedimientoAnalisis procedimiento =
                procedimientos
                        .findFirstByCodigoAndActivoTrueOrderByVersionDesc(CODIGO_SOFTWARE_IA)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Procedimiento SOFTWARE_IA activo"));
        Contexto contexto =
                validarContexto(
                        request.clienteId(),
                        procedimiento,
                        request.participacionEstudioId(),
                        authentication);
        EventoAnalisis evento =
                lifecycle.crear(
                        contexto.cliente(),
                        procedimiento,
                        request.fechaCorte(),
                        request.momento(),
                        contexto.participacion(),
                        contexto.evaluador());

        EjecucionSoftwareIa ejecucion = ejecutarSoftwareIa(evento, true);
        var ciclo = cicloPostPrediccion == null
                ? null
                : cicloPostPrediccion.procesar(ejecucion.resultado().prediccion());
        return AnalisisPredictivoResponse.from(
                ejecucion.evento(), ejecucion.resultado().prediccion(),
                ciclo == null ? null : ciclo.pccIa(), ciclo == null ? null : ciclo.pcs());
    }

    public AnalisisPredictivoResponse predecirInstrumentadoV6(
            AnalisisPredictivoRequest request, Authentication authentication) {
        long inicioActivo = System.nanoTime();
        ProcedimientoAnalisis procedimiento =
                procedimientos
                        .findFirstByCodigoAndActivoTrueOrderByVersionDesc(CODIGO_SOFTWARE_IA)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Procedimiento SOFTWARE_IA activo"));
        Contexto contexto =
                validarContexto(
                        request.clienteId(),
                        procedimiento,
                        request.participacionEstudioId(),
                        authentication);
        EventoAnalisis evento =
                lifecycle.crear(
                        contexto.cliente(),
                        procedimiento,
                        request.fechaCorte(),
                        request.momento(),
                        contexto.participacion(),
                        contexto.evaluador());

        EjecucionSoftwareIa ejecucion = ejecutarSoftwareIaV6(evento, true);
        var ciclo = cicloPostPrediccion == null ? null
                : cicloPostPrediccion.procesar(ejecucion.resultado().prediccion());
        EventoAnalisis eventoFinal = ejecucion.evento();
        if (ciclo != null && request.momento() == com.backend.nutri_predic.common.enums.MomentoEvaluacion.DIARIO) {
            long duracionMs = Math.max(0L, (System.nanoTime() - inicioActivo) / 1_000_000L);
            eventoFinal = lifecycle.registrarIntentoCiclo(
                    ejecucion.evento().getId(), duracionMs, ciclo.completo(),
                    ciclo.moduloFallo(), ciclo.motivoFallo());
        }
        return AnalisisPredictivoResponse.from(
                eventoFinal, ejecucion.resultado().prediccion(),
                ciclo == null ? null : ciclo.pccIa(), ciclo == null ? null : ciclo.pcs());
    }

    private EjecucionSoftwareIa ejecutarSoftwareIa(
            EventoAnalisis evento, boolean propagarFalloEsperado) {
        ResultadoInferenciaModelo resultado = null;
        TrazabilidadInferencia trazabilidad =
                new TrazabilidadInferencia(
                        instante ->
                                lifecycle.registrarVariablesPreparadas(evento.getId(), instante),
                        instante -> lifecycle.registrarModeloSolicitado(evento.getId(), instante),
                        instante -> lifecycle.registrarModeloRespondio(evento.getId(), instante));
        try {
            resultado =
                    modeloPredictivo.predecirConTrazabilidad(
                            new AnalisisPredictivoRequest(
                                    evento.getCliente().getId(),
                                    evento.getFechaCorte(),
                                    evento.getParticipacionEstudio() == null
                                            ? null
                                            : evento.getParticipacionEstudio().getId(),
                                    evento.getMomento()),
                            trazabilidad);
            EventoAnalisis cerrado =
                    lifecycle.cerrarExitoso(
                            evento.getId(),
                            resultado.prediccion().getId(),
                            resultado.origenResultado());
            return new EjecucionSoftwareIa(cerrado, resultado);
        } catch (ModeloMlException | BusinessException error) {
            Long prediccionId = idPrediccionFallida(error);
            EventoAnalisis fallido =
                    invalidarConservandoExcepcion(
                            evento.getId(), motivoEsperado(error), prediccionId, null, error);
            if (propagarFalloEsperado) {
                throw error;
            }
            return new EjecucionSoftwareIa(fallido, null);
        } catch (RuntimeException error) {
            Long prediccionId = resultado == null ? null : resultado.prediccion().getId();
            OrigenResultadoAnalisis origen = resultado == null ? null : resultado.origenResultado();
            invalidarConservandoExcepcion(
                    evento.getId(),
                    "Error inesperado durante el análisis predictivo",
                    prediccionId,
                    origen,
                    error);
            throw error;
        }
    }

    private EjecucionSoftwareIa ejecutarSoftwareIaV6(
            EventoAnalisis evento, boolean propagarFalloEsperado) {
        ResultadoInferenciaModelo resultado = null;
        TrazabilidadInferencia trazabilidad =
                new TrazabilidadInferencia(
                        instante ->
                                lifecycle.registrarVariablesPreparadas(evento.getId(), instante),
                        instante -> lifecycle.registrarModeloSolicitado(evento.getId(), instante),
                        instante -> lifecycle.registrarModeloRespondio(evento.getId(), instante));
        try {
            resultado =
                    modeloPredictivoV6.predecirConTrazabilidad(
                            new AnalisisPredictivoRequest(
                                    evento.getCliente().getId(),
                                    evento.getFechaCorte(),
                                    evento.getParticipacionEstudio() == null
                                            ? null
                                            : evento.getParticipacionEstudio().getId(),
                                    evento.getMomento()),
                            trazabilidad);
            EventoAnalisis cerrado =
                    lifecycle.cerrarExitoso(
                            evento.getId(),
                            resultado.prediccion().getId(),
                            resultado.origenResultado());
            return new EjecucionSoftwareIa(cerrado, resultado);
        } catch (ModeloMlException | BusinessException error) {
            Long prediccionId = idPrediccionFallida(error);
            EventoAnalisis fallido =
                    invalidarConservandoExcepcion(
                            evento.getId(), motivoEsperado(error), prediccionId, null, error);
            if (propagarFalloEsperado) {
                throw error;
            }
            return new EjecucionSoftwareIa(fallido, null);
        } catch (RuntimeException error) {
            Long prediccionId = resultado == null ? null : resultado.prediccion().getId();
            OrigenResultadoAnalisis origen = resultado == null ? null : resultado.origenResultado();
            invalidarConservandoExcepcion(
                    evento.getId(),
                    "Error inesperado durante el análisis predictivo V6",
                    prediccionId,
                    origen,
                    error);
            throw error;
        }
    }

    private EventoAnalisis invalidarConservandoExcepcion(
            Long eventoId,
            String motivo,
            Long prediccionId,
            OrigenResultadoAnalisis origen,
            RuntimeException original) {
        try {
            return lifecycle.marcarInvalido(eventoId, motivo, prediccionId, origen);
        } catch (RuntimeException errorPersistencia) {
            original.addSuppressed(errorPersistencia);
            throw original;
        }
    }

    private Long idPrediccionFallida(RuntimeException error) {
        return error instanceof PrediccionModeloFallidaException fallo
                ? fallo.getPrediccionModeloId()
                : null;
    }

    private String motivoEsperado(RuntimeException error) {
        return error instanceof ModeloMlException
                ? "Error técnico al consultar el modelo"
                : "Datos técnicamente insuficientes para el modelo";
    }

    private Contexto validarContexto(
            Long clienteId,
            ProcedimientoAnalisis procedimiento,
            Long participacionId,
            Authentication authentication) {
        Cliente cliente = access.client(clienteId, authentication);
        if (!procedimiento.isActivo()) {
            throw new BusinessException("Procedimiento inactivo");
        }
        ParticipacionEstudio participacion =
                participacionId == null
                        ? null
                        : participaciones
                                .findById(participacionId)
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Participación de estudio"));
        if (participacion != null
                && (!participacion.getCliente().getId().equals(cliente.getId())
                        || participacion.getEstado() != EstadoEstudio.ACTIVO)) {
            throw new BusinessException("La participación no es válida para el cliente");
        }
        Usuario evaluador = usuarios.findByEmail(authentication.getName()).orElse(null);
        return new Contexto(cliente, participacion, evaluador);
    }

    public EventoAnalisisResponse finalizar(
            Long id, FinalizarAnalisisRequest request, Authentication authentication) {
        EventoAnalisis evento =
                eventos.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Evento de análisis"));
        access.client(evento.getCliente().getId(), authentication);
        if (evento.getProcedimiento().getTipo() == TipoProcedimientoAnalisis.SOFTWARE_IA) {
            throw new BusinessException("El análisis SOFTWARE_IA se finaliza automáticamente");
        }
        if (evento.getResultadoDisponibleEn() != null) {
            throw new BusinessException("El análisis ya fue finalizado");
        }
        return EventoAnalisisResponse.from(
                lifecycle.cerrarManual(id, request.observacionesTecnicas()));
    }

    @Transactional(readOnly = true)
    public EventoAnalisisResponse get(Long id, Authentication authentication) {
        EventoAnalisis evento =
                eventos.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Evento de análisis"));
        access.client(evento.getCliente().getId(), authentication);
        return EventoAnalisisResponse.from(evento);
    }

    private record Contexto(
            Cliente cliente, ParticipacionEstudio participacion, Usuario evaluador) {}

    private record EjecucionSoftwareIa(
            EventoAnalisis evento, ResultadoInferenciaModelo resultado) {}
}
