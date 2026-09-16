package com.backend.nutri_predic.prediccionmodelo.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.DatosV6IncompletosException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.estudio.entity.EstadoEstudio;
import com.backend.nutri_predic.estudio.repository.ParticipacionEstudioRepository;
import com.backend.nutri_predic.ml.client.ModeloMlClient;
import com.backend.nutri_predic.ml.dto.MlPredictRequest;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoRequest;
import com.backend.nutri_predic.prediccionmodelo.dto.PrediccionModeloHistorialResponse;
import com.backend.nutri_predic.prediccionmodelo.dto.PreparacionAnalisisPredictivoV6Response;
import com.backend.nutri_predic.prediccionmodelo.dto.DominioPreparacionAnalisisResponse;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6;
import com.backend.nutri_predic.variablemodelov6.schema.FeatureSchemaV6Mapper;
import com.backend.nutri_predic.variablemodelov6.service.VariablesModeloV6Service;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModeloPredictivoV6Service {
    public static final String MODEL_VERSION_ESPERADA = "technical-v6-daily-002";
    public static final String MODEL_VERSION_FINAL_PREFIX = "random-forest-v6-final-";
    private static final String MODEL_TYPE = "LOGISTIC_REGRESSION";
    private static final String TRAINING_DATA_TYPE = "SYNTHETIC_TECHNICAL";
    private static final boolean THESIS_FINAL_MODEL = false;
    private static final List<String> FEATURES_ESENCIALES = FeatureSchemaV6Mapper.FEATURE_NAMES;

    private final ClienteRepository clientes;
    private final ParticipacionEstudioRepository participaciones;
    private final VariablesModeloV6Service variables;
    private final FeatureSchemaV6Mapper featureMapper;
    private final ModeloMlClient clienteMl;
    private final PrediccionModeloRepository predicciones;

    public ModeloPredictivoV6Service(
            ClienteRepository clientes,
            ParticipacionEstudioRepository participaciones,
            VariablesModeloV6Service variables,
            FeatureSchemaV6Mapper featureMapper,
            ModeloMlClient clienteMl,
            PrediccionModeloRepository predicciones) {
        this.clientes = clientes;
        this.participaciones = participaciones;
        this.variables = variables;
        this.featureMapper = featureMapper;
        this.clienteMl = clienteMl;
        this.predicciones = predicciones;
    }

    @Transactional(noRollbackFor = {ModeloMlException.class, BusinessException.class})
    public ResultadoInferenciaModelo predecirConTrazabilidad(
            AnalisisPredictivoRequest request, TrazabilidadInferencia trazabilidad) {
        validarFechaCorte(request.fechaCorte());
        var cliente =
                clientes.findById(request.clienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var participacion =
                request.participacionEstudioId() == null
                        ? null
                        : participaciones
                                .findById(request.participacionEstudioId())
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Participación de estudio"));
        if (participacion != null
                && (!participacion.getCliente().getId().equals(cliente.getId())
                        || participacion.getEstado() != EstadoEstudio.ACTIVO)) {
            throw new BusinessException("La participación no es válida para el cliente");
        }

        var observacion = variables.construir(cliente.getId(), request.fechaCorte());
        var preparacion = evaluarPreparacion(cliente.getId(), request.fechaCorte(), observacion);
        if (!preparacion.puedeAnalizar()) {
            throw new DatosV6IncompletosException(preparacion.datosFaltantes());
        }

        var reutilizable =
                predicciones
                        .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndParticipacionEstudioIdAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                                cliente.getId(),
                                request.fechaCorte(),
                                request.momento(),
                                participacion == null ? null : participacion.getId(),
                                FeatureSchemaV6Mapper.SCHEMA_VERSION,
                                EstadoPrediccionModelo.EXITOSA)
                        .filter(
                                prediccion ->
                                        esModeloDiarioAdmitido(prediccion.getModelVersion())
                                                &&
                                        prediccion.getInferenceMs() != null
                                                && prediccion.getInferredAt() != null
                                                && prediccion.getMetaKcal() != null
                                                && prediccion.getMetaProteinaG() != null
                                                && prediccion.getMetaCarbohidratosG() != null
                                                && prediccion.getMetaGrasasG() != null
                                                && prediccion.getMetaAguaMl() != null);
        if (reutilizable.isPresent())
            return new ResultadoInferenciaModelo(reutilizable.get(), OrigenResultadoAnalisis.REUTILIZADO);

        trazabilidad.variablesPreparadas().accept(Instant.now());
        trazabilidad.modeloSolicitado().accept(Instant.now());
        try {
            var respuesta =
                    clienteMl.predecir(MlPredictRequest.from(observacion.schema()));
            trazabilidad.modeloRespondio().accept(Instant.now());

            validarRespuestaV6(respuesta);

            var prediccion = new PrediccionModelo();
            prediccion.setCliente(cliente);
            prediccion.setParticipacionEstudio(participacion);
            prediccion.setMomentoEvaluacion(request.momento());
            prediccion.setFechaCorte(request.fechaCorte());
            prediccion.setClasificacionPredicha(
                    ClasificacionPerfilHabitos.valueOf(respuesta.clasificacion()));
            prediccion.setProbAdecuado(respuesta.probabilidades().adecuado());
            prediccion.setProbMejorable(respuesta.probabilidades().mejorable());
            prediccion.setProbCritico(respuesta.probabilidades().critico());
            prediccion.setModelVersion(respuesta.modelVersion());
            prediccion.setSchemaVersion(respuesta.schemaVersion());
            prediccion.setInferenceMs(respuesta.inferenceMs());
            prediccion.setInferredAt(respuesta.inferredAt());
            boolean finalRf = esModeloFinalRandomForest(respuesta.modelVersion());
            prediccion.setModelType(finalRf ? "RANDOM_FOREST" : MODEL_TYPE);
            prediccion.setTrainingDataType(finalRf ? "REAL" : TRAINING_DATA_TYPE);
            prediccion.setThesisFinalModel(finalRf);
            prediccion.setFeatureCount(FeatureSchemaV6Mapper.FEATURE_NAMES.size());
            prediccion.setMetaKcal(respuesta.kcal());
            prediccion.setMetaProteinaG(respuesta.proteinaG());
            prediccion.setMetaCarbohidratosG(respuesta.carbohidratosG());
            prediccion.setMetaGrasasG(respuesta.grasasG());
            prediccion.setMetaAguaMl(respuesta.aguaMl());
            prediccion.setFormulaNutricionalVersion(respuesta.formulaNutricionalVersion());
            prediccion.setFuenteFormulaNutricional(respuesta.fuenteFormulaNutricional());
            prediccion.setEstado(EstadoPrediccionModelo.EXITOSA);
            return new ResultadoInferenciaModelo(
                    predicciones.saveAndFlush(prediccion), OrigenResultadoAnalisis.GENERADO);
        } catch (ModeloMlException error) {
            trazabilidad.modeloRespondio().accept(Instant.now());
            var fallo = new PrediccionModelo();
            fallo.setCliente(cliente);
            fallo.setParticipacionEstudio(participacion);
            fallo.setMomentoEvaluacion(request.momento());
            fallo.setFechaCorte(request.fechaCorte());
            fallo.setSchemaVersion(FeatureSchemaV6Mapper.SCHEMA_VERSION);
            fallo.setEstado(EstadoPrediccionModelo.FALLIDA);
            fallo.setMensajeError("Error técnico al consultar el modelo");
            fallo = predicciones.saveAndFlush(fallo);
            throw new PrediccionModeloFallidaException(
                    "No se pudo obtener una predicción del modelo", fallo.getId(), error);
        }
    }

    @Transactional(readOnly = true)
    public List<PrediccionModeloHistorialResponse> historial(Long clienteId) {
        if (!clientes.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente");
        }
        return predicciones
                .findByClienteIdAndEstadoOrderByFechaPrediccionDesc(
                        clienteId, EstadoPrediccionModelo.EXITOSA)
                .stream()
                .filter(prediccion -> esModeloDiarioAdmitido(prediccion.getModelVersion()))
                .map(PrediccionModeloHistorialResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PreparacionAnalisisPredictivoV6Response preparacion(Long clienteId, java.time.LocalDate fechaCorte) {
        validarFechaCorte(fechaCorte);
        if (!clientes.existsById(clienteId)) throw new ResourceNotFoundException("Cliente");
        
        return evaluarPreparacion(clienteId, fechaCorte, variables.construir(clienteId, fechaCorte));
    }

    private PreparacionAnalisisPredictivoV6Response evaluarPreparacion(
            Long clienteId,
            java.time.LocalDate fechaCorte,
            VariablesModeloV6Service.Observacion observacion) {
        var faltantes = new java.util.ArrayList<String>();
        var dominios = new java.util.LinkedHashMap<String, DominioPreparacionAnalisisResponse>();
        if (!observacion.perfilHistorico()) {
            faltantes.add("perfil historico no disponible o no aplicable al corte");
        }
        if (!observacion.registroAnteriorCompleto()) {
            faltantes.add("No existe un registro completo del día anterior: se requieren metas, al menos un alimento con macronutrientes y la cantidad de agua consumida");
        }
        dominios.put("REGISTRO_DIARIO_ANTERIOR", new DominioPreparacionAnalisisResponse(
                observacion.registroAnteriorCompleto() ? "COMPLETO" : "INCOMPLETO",
                observacion.registroAnteriorCompleto() ? 1 : 0, 1));
        FEATURES_ESENCIALES.forEach(nombre -> {
            if (observacion.schema().features().get(nombre) == null)
                faltantes.add("Dato esencial sin valor: " + nombre);
        });
        int disponibles = (int) FEATURES_ESENCIALES.stream()
                .filter(nombre -> observacion.schema().features().get(nombre) != null).count();
        return new PreparacionAnalisisPredictivoV6Response(
                clienteId, fechaCorte, faltantes.isEmpty(), observacion.registroAnteriorCompleto() ? 1 : 0, 1,
                java.util.Map.copyOf(dominios), observacion.perfilHistorico(),
                disponibles, FEATURES_ESENCIALES.size(),
                List.copyOf(faltantes));
    }

    private void validarRespuestaV6(com.backend.nutri_predic.ml.dto.MlPredictResponse respuesta) {
        if (!FeatureSchemaV6Mapper.SCHEMA_VERSION.equals(respuesta.schemaVersion())) {
            throw new ModeloMlException(
                    "SchemaVersion incompatible: esperado " + FeatureSchemaV6Mapper.SCHEMA_VERSION
                            + ", recibido " + respuesta.schemaVersion());
        }
        if (!esModeloDiarioAdmitido(respuesta.modelVersion())) {
            throw new ModeloMlException(
                    "ModelVersion incompatible: esperado " + MODEL_VERSION_ESPERADA + " o Random Forest final"
                            + ", recibido " + respuesta.modelVersion());
        }
        if (respuesta.inferenceMs() == null
                || respuesta.inferenceMs().compareTo(java.math.BigDecimal.ZERO) < 0
                || respuesta.inferredAt() == null) {
            throw new ModeloMlException("Respuesta V6 sin metadata de inferencia válida");
        }
        if (!"DISPONIBLE".equals(respuesta.metasEstado())
                || respuesta.kcal() == null || respuesta.kcal().signum() <= 0
                || respuesta.proteinaG() == null || respuesta.proteinaG().signum() < 0
                || respuesta.carbohidratosG() == null || respuesta.carbohidratosG().signum() < 0
                || respuesta.grasasG() == null || respuesta.grasasG().signum() < 0
                || respuesta.aguaMl() == null || respuesta.aguaMl().signum() <= 0
                || respuesta.formulaNutricionalVersion() == null
                || respuesta.fuenteFormulaNutricional() == null) {
            throw new ModeloMlException("Respuesta V6 sin metas nutricionales válidas: "
                    + respuesta.motivoMetasNoDisponibles());
        }
    }

    private void validarFechaCorte(java.time.LocalDate fechaCorte) {
        if (fechaCorte == null || fechaCorte.isAfter(
                java.time.LocalDate.now(java.time.ZoneId.of("America/Lima"))))
            throw new BusinessException("fechaCorte no puede ser futura");
    }

    public static boolean esModeloFinalRandomForest(String version) {
        return version != null && version.startsWith(MODEL_VERSION_FINAL_PREFIX);
    }

    public static boolean esModeloDiarioAdmitido(String version) {
        return MODEL_VERSION_ESPERADA.equals(version) || esModeloFinalRandomForest(version);
    }
}
