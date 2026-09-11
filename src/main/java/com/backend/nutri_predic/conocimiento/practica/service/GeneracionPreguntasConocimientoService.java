package com.backend.nutri_predic.conocimiento.practica.service;

import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.conocimiento.dto.InstrumentoPublicoResponse;
import com.backend.nutri_predic.conocimiento.entity.EstadoInstrumento;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoConocimientoRepository;
import com.backend.nutri_predic.conocimiento.repository.InstrumentoPreguntaRepository;
import com.backend.nutri_predic.conocimiento.gemini.client.GeminiClient;
import com.backend.nutri_predic.conocimiento.gemini.client.GeminiClientException;
import com.backend.nutri_predic.conocimiento.gemini.config.GeminiProperties;
import com.backend.nutri_predic.conocimiento.practica.dto.GenerarConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.dto.SesionConocimientoPublicaResponse;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.entity.PreguntaGeneradaIa;
import com.backend.nutri_predic.conocimiento.practica.entity.SesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.gemini.entity.TrazaLlamadaGemini;
import com.backend.nutri_predic.conocimiento.practica.repository.PreguntaGeneradaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.RespuestaAdaptativaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.conocimiento.gemini.repository.TrazaLlamadaGeminiRepository;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.plandia.entity.EstadoPlanDiario;
import com.backend.nutri_predic.plandia.entity.PlanDiario;
import com.backend.nutri_predic.plandia.repository.PlanDiarioRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
public class GeneracionPreguntasConocimientoService {
    private static final String CONFIG = "pcc-ia-v1";
    private static final Logger log =
            LoggerFactory.getLogger(GeneracionPreguntasConocimientoService.class);

    private final PrediccionModeloRepository predicciones;
    private final SesionConocimientoIaRepository sesiones;
    private final PreguntaGeneradaIaRepository preguntas;
    private final RespuestaAdaptativaIaRepository respuestas;
    private final TrazaLlamadaGeminiRepository trazas;
    private final InstrumentoConocimientoRepository instrumentos;
    private final InstrumentoPreguntaRepository instrumentoPreguntas;
    private final GeminiClient gemini;
    private final GeminiProperties props;
    private final AccessService access;
    private final PlanDiarioRepository planes;

    public GeneracionPreguntasConocimientoService(
            PrediccionModeloRepository predicciones,
            SesionConocimientoIaRepository sesiones,
            PreguntaGeneradaIaRepository preguntas,
            RespuestaAdaptativaIaRepository respuestas,
            TrazaLlamadaGeminiRepository trazas,
            InstrumentoConocimientoRepository instrumentos,
            InstrumentoPreguntaRepository instrumentoPreguntas,
            GeminiClient gemini,
            GeminiProperties props,
            AccessService access,
            PlanDiarioRepository planes) {
        this.predicciones = predicciones;
        this.sesiones = sesiones;
        this.preguntas = preguntas;
        this.respuestas = respuestas;
        this.trazas = trazas;
        this.instrumentos = instrumentos;
        this.instrumentoPreguntas = instrumentoPreguntas;
        this.gemini = gemini;
        this.props = props;
        this.access = access;
        this.planes = planes;
    }

    @Transactional
    public SesionConocimientoPublicaResponse generar(
            Long clienteId, GenerarConocimientoIaRequest request, Authentication auth) {
        return generarConConfiguracion(clienteId, request, auth, CONFIG, null, null);
    }

    /** Disparo del ciclo V5: siempre usa la misma configuración para una predicción. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SesionConocimientoPublicaResponse generarAutomatico(Long prediccionId) {
        var prediccion = predicciones.findById(prediccionId)
                .orElseThrow(() -> new ResourceNotFoundException("Predicción de modelo"));
        var sesionDiaria = sesiones
                .findFirstByClienteIdAndFechaEvaluacionOrderByCreadoEnDesc(
                        prediccion.getCliente().getId(), prediccion.getFechaCorte());
        if (sesionDiaria.isPresent()) return respuesta(sesionDiaria.get());
        if (prediccion.getEstado() != EstadoPrediccionModelo.EXITOSA
                || !(prediccion.getSchemaVersion().startsWith("variables-modelo-v5")
                        || prediccion.getSchemaVersion().startsWith("variables-modelo-v6")))
            throw new IllegalArgumentException("La evaluación de conocimiento requiere una predicción exitosa");
        return generarConConfiguracion(
                prediccion.getCliente().getId(),
                new GenerarConocimientoIaRequest(List.of("ALIMENTACION", "SUPLEMENTACION", "HABITOS"), "MEDIA", 5),
                null, CONFIG, prediccion, null);
    }

    @Transactional
    public SesionConocimientoPublicaResponse generarRetryAdministrativo(
            Long sesionId, GenerarConocimientoIaRequest request, Authentication auth) {
        var origen =
                sesiones.findById(sesionId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "No existe la sesión PCC-IA indicada"));
        access.client(origen.getPrediccionModelo().getCliente().getId(), auth);
        if (origen.getEstado() != EstadoSesionConocimientoIa.IA_NO_DISPONIBLE
                || !"GEMINI".equals(origen.getProveedorIa())
                || !origen.getSchemaVersion().startsWith("variables-modelo-v5"))
            throw new ResourceNotFoundException("La sesión PCC-IA indicada no es retryable");
        return generarConConfiguracion(
                origen.getPrediccionModelo().getCliente().getId(),
                request,
                auth,
                "pcc-ia-retry-sesion-" + sesionId,
                origen.getPrediccionModelo(),
                origen);
    }

    private SesionConocimientoPublicaResponse generarConConfiguracion(
            Long clienteId,
            GenerarConocimientoIaRequest request,
            Authentication auth,
            String configuracionVersion,
            PrediccionModelo prediccionForzada,
            SesionConocimientoIa reintentoDeSesion) {
        if (auth != null) access.client(clienteId, auth);
        var prediccion =
                prediccionForzada != null
                        ? prediccionForzada
                        : predicciones
                                .findByClienteIdAndEstadoOrderByFechaPrediccionDesc(
                                        clienteId, EstadoPrediccionModelo.EXITOSA)
                                .stream()
                                .filter(p -> p.getSchemaVersion().startsWith("variables-modelo-v5"))
                                .findFirst()
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "No existe una predicción V5 exitosa"));
        var existente =
                sesiones.findByPrediccionModeloIdAndConfiguracionVersion(
                        prediccion.getId(), configuracionVersion);
        if (existente.isPresent()) {
            trazas.save(
                    new TrazaLlamadaGemini(
                            prediccion,
                            existente.get().getEstado()
                                    != EstadoSesionConocimientoIa.IA_NO_DISPONIBLE,
                            preguntas.findBySesionIdOrderByOrdenAsc(existente.get().getId()).size(),
                            true));
            return respuesta(existente.get());
        }

        var plan = prediccion.getFechaCorte() == null
                ? null
                : planes.findByClienteIdAndFechaObjetivo(
                        clienteId, prediccion.getFechaCorte()).orElse(null);
        var sesion = nuevaSesion(prediccion, configuracionVersion, plan);
        sesion.setReintentoDeSesion(reintentoDeSesion);
        try {
            var generadas =
                    gemini.generar(
                            new GeminiClient.Solicitud(
                                    request.temasPermitidos(),
                                    request.dificultadPermitida(),
                                    request.cantidadPreguntas(),
                                    objetivoCliente(prediccion),
                                    prediccion.getClasificacionPredicha() == null
                                            ? null
                                            : prediccion.getClasificacionPredicha().name(),
                                    contextoPlan(plan),
                                    contextoErroresPrevios(clienteId, prediccion.getFechaCorte())));
            validar(generadas, request);
            sesion.setEstado(EstadoSesionConocimientoIa.GENERADA);
            sesion.setGeneradoEn(Instant.now());
            sesion = sesiones.save(sesion);
            int orden = 1;
            for (var generada : generadas) {
                var pregunta = new PreguntaGeneradaIa();
                pregunta.setSesion(sesion);
                pregunta.setTema(generada.tema().trim());
                pregunta.setSubtema(generada.subtema().trim());
                pregunta.setDificultad(generada.dificultad().trim());
                pregunta.setEnunciado(generada.enunciado().trim());
                pregunta.setOpcionA(generada.opciones().get(0).texto().trim());
                pregunta.setOpcionB(generada.opciones().get(1).texto().trim());
                pregunta.setOpcionC(generada.opciones().get(2).texto().trim());
                pregunta.setOpcionD(generada.opciones().get(3).texto().trim());
                pregunta.setRespuestaCorrecta(generada.respuestaCorrecta().trim().toUpperCase());
                pregunta.setExplicacion(generada.explicacion().trim());
                pregunta.setOrden(orden++);
                preguntas.save(pregunta);
            }
            trazas.save(new TrazaLlamadaGemini(prediccion, true, generadas.size(), false));
            return respuesta(sesion);
        } catch (GeminiClientException error) {
            return respuesta(
                    registrarFallo(
                            sesion,
                            prediccion,
                            error.getCodigoTecnico(),
                            error.getHttpStatus(),
                            error.getEtapaError(),
                            error.getTipoExcepcionSeguro()));
        } catch (Exception error) {
            return respuesta(
                    registrarFallo(
                            sesion,
                            prediccion,
                            "GEMINI_VALIDATION_FAILED",
                            null,
                            "VALIDATE_GENERATED_QUESTIONS",
                            error.getClass().getSimpleName()));
        }
    }

    @Transactional(readOnly = true)
    public SesionConocimientoPublicaResponse obtener(Long clienteId, Authentication auth) {
        return obtener(clienteId, null, auth);
    }

    @Transactional(readOnly = true)
    public SesionConocimientoPublicaResponse obtener(
            Long clienteId, java.time.LocalDate fecha, Authentication auth) {
        access.client(clienteId, auth);
        var sesion =
                (fecha == null
                        ? sesiones.findFirstByPrediccionModeloClienteIdOrderByCreadoEnDesc(clienteId)
                        : sesiones.findFirstByClienteIdAndFechaEvaluacionOrderByCreadoEnDesc(clienteId, fecha))
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "No existe una evaluación de conocimiento para la fecha indicada"));
        return respuesta(sesion);
    }

    private SesionConocimientoIa nuevaSesion(
            PrediccionModelo prediccion, String configuracionVersion, PlanDiario plan) {
        var sesion = new SesionConocimientoIa();
        sesion.setPrediccionModelo(prediccion);
        sesion.setConfiguracionVersion(configuracionVersion);
        sesion.setModelVersionPredictivo(prediccion.getModelVersion());
        sesion.setSchemaVersion(prediccion.getSchemaVersion());
        sesion.setModeloGenerativo(props.getModel());
        sesion.setProveedorIa("GEMINI");
        sesion.setFechaEvaluacion(prediccion.getFechaCorte());
        sesion.setClienteId(prediccion.getCliente().getId());
        sesion.setObjetivoCliente(objetivoCliente(prediccion));
        sesion.setClasificacionPredictiva(
                prediccion.getClasificacionPredicha() == null
                        ? null
                        : prediccion.getClasificacionPredicha().name());
        if (plan != null && plan.getEstado() == EstadoPlanDiario.DISPONIBLE) {
            sesion.setPlanDiario(plan);
            sesion.setMetaKcal(plan.getEnergiaMaxKcal());
            sesion.setMetaProteinaG(plan.getProteinaMaxG());
            sesion.setMetaCarbohidratosG(plan.getCarbohidratosMaxG());
            sesion.setMetaGrasasG(plan.getGrasasMaxG());
            sesion.setMetaAguaMl(plan.getAguaMaxMl());
        }
        sesion.setPuntajeMaximo(java.math.BigDecimal.TEN);
        sesion.setInstrumento(
                instrumentos
                        .findFirstByEstadoOrderByVigenteDesdeDescIdDesc(EstadoInstrumento.ACTIVO)
                        .orElse(null));
        return sesion;
    }

    private String contextoPlan(PlanDiario plan) {
        if (plan == null || plan.getEstado() != EstadoPlanDiario.DISPONIBLE) return "NO_DISPONIBLE";
        return "fecha=" + plan.getFechaObjetivo()
                + ", kcal=" + plan.getEnergiaMaxKcal()
                + ", proteinaG=" + plan.getProteinaMaxG()
                + ", carbohidratosG=" + plan.getCarbohidratosMaxG()
                + ", grasasG=" + plan.getGrasasMaxG()
                + ", aguaMl=" + plan.getAguaMaxMl();
    }

    private String contextoErroresPrevios(Long clienteId, java.time.LocalDate fechaActual) {
        if (fechaActual == null) return "NO_DISPONIBLE";
        var anterior = sesiones
                .findFirstByClienteIdAndEstadoAndFechaEvaluacionBeforeOrderByFechaEvaluacionDescCreadoEnDesc(
                        clienteId, EstadoSesionConocimientoIa.RESPONDIDA, fechaActual)
                .orElse(null);
        if (anterior == null) return "PRIMERA_EVALUACION";
        var fallos = respuestas.findBySesionIdOrderByPreguntaGeneradaIaOrdenAsc(anterior.getId()).stream()
                .filter(respuesta -> !respuesta.isCorrecta())
                .map(respuesta -> {
                    var pregunta = respuesta.getPreguntaGeneradaIa();
                    return "tema=" + pregunta.getTema() + ", subtema=" + pregunta.getSubtema()
                            + ", explicacion=" + pregunta.getExplicacion();
                })
                .toList();
        return fallos.isEmpty() ? "SIN_ERRORES_EN_LA_EVALUACION_ANTERIOR" : String.join(" | ", fallos);
    }

    private String objetivoCliente(PrediccionModelo prediccion) {
        var cliente = prediccion.getCliente();
        var partes = new java.util.ArrayList<String>();
        if (cliente.getTipoObjetivoFisico() != null) partes.add(cliente.getTipoObjetivoFisico().name());
        if (cliente.getObjetivoEnergetico() != null) partes.add(cliente.getObjetivoEnergetico().name());
        if (cliente.getObjetivoFisico() != null && !cliente.getObjetivoFisico().isBlank())
            partes.add(cliente.getObjetivoFisico().trim());
        return partes.isEmpty() ? "NO_DECLARADO" : String.join(" | ", partes);
    }

    private SesionConocimientoIa registrarFallo(
            SesionConocimientoIa sesion,
            PrediccionModelo prediccion,
            String codigoTecnico,
            Integer httpStatus,
            String etapaError,
            String tipoExcepcionSeguro) {
        sesion.setEstado(EstadoSesionConocimientoIa.IA_NO_DISPONIBLE);
        sesion.setCodigoErrorTecnico(codigoTecnico);
        sesion.setHttpStatusGemini(httpStatus);
        sesion.setFallidoEn(Instant.now());
        sesion.setEtapaError(etapaError);
        sesion.setTipoExcepcionSeguro(tipoExcepcionSeguro);
        var persistida = sesiones.save(sesion);
        trazas.save(new TrazaLlamadaGemini(prediccion, false, 0, false));
        log.warn(
                "Fallo Gemini prediccionModeloId={} sesionId={} codigoErrorTecnico={} httpStatusGemini={} etapaError={} tipoExcepcionSeguro={} timestamp={}",
                prediccion.getId(),
                persistida.getId(),
                codigoTecnico,
                httpStatus,
                etapaError,
                tipoExcepcionSeguro,
                persistida.getFallidoEn());
        return persistida;
    }

    private SesionConocimientoPublicaResponse respuesta(SesionConocimientoIa sesion) {
        InstrumentoPublicoResponse instrumento = null;
        if (sesion.getInstrumento() != null) {
            instrumento =
                    InstrumentoPublicoResponse.from(
                            sesion.getInstrumento(),
                            instrumentoPreguntas.findByInstrumentoIdOrderByOrdenAsc(
                                    sesion.getInstrumento().getId()));
        }
        return SesionConocimientoPublicaResponse.from(
                sesion,
                instrumento,
                preguntas.findBySesionIdOrderByOrdenAsc(sesion.getId()),
                respuestas.findBySesionIdOrderByPreguntaGeneradaIaOrdenAsc(sesion.getId()));
    }

    private void validar(
            List<GeminiClient.Respuesta.Pregunta> generadas, GenerarConocimientoIaRequest request) {
        if (generadas == null) {
            throw validacionInvalida("GEMINI_SCHEMA_INVALID");
        }
        if (generadas.size() != request.cantidadPreguntas()) {
            throw validacionInvalida("GEMINI_QUESTION_COUNT_INVALID");
        }
        for (var pregunta : generadas) {
            if (blank(pregunta.tema())
                    || blank(pregunta.subtema())
                    || blank(pregunta.enunciado())
                    || blank(pregunta.explicacion())
                    || blank(pregunta.respuestaCorrecta())) {
                throw validacionInvalida("GEMINI_SCHEMA_INVALID");
            }
            if (!request.temasPermitidos().contains(pregunta.tema())) {
                throw validacionInvalida("GEMINI_TOPIC_NOT_ALLOWED");
            }
            if (!request.dificultadPermitida().equalsIgnoreCase(pregunta.dificultad())) {
                throw validacionInvalida("GEMINI_DIFFICULTY_INVALID");
            }
            if (pregunta.opciones() == null || pregunta.opciones().size() != 4) {
                throw validacionInvalida("GEMINI_OPTIONS_INVALID");
            }
            var codigos =
                    pregunta.opciones().stream()
                            .map(
                                    opcion ->
                                            opcion.codigo() == null
                                                    ? ""
                                                    : opcion.codigo().trim().toUpperCase())
                            .toList();
            if (!new HashSet<>(codigos).equals(Set.of("A", "B", "C", "D"))
                    || !codigos.contains(pregunta.respuestaCorrecta().trim().toUpperCase())
                    || pregunta.opciones().stream().anyMatch(opcion -> blank(opcion.texto()))) {
                throw validacionInvalida("GEMINI_OPTIONS_INVALID");
            }
        }
    }

    private boolean blank(String valor) {
        return valor == null || valor.isBlank();
    }

    private GeminiClientException validacionInvalida(String codigo) {
        return new GeminiClientException(
                codigo, 200, null, "VALIDATE_GENERATED_QUESTIONS", "ValidationException");
    }
}
