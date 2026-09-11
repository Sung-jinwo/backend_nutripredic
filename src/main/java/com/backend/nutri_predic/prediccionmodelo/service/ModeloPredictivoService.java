package com.backend.nutri_predic.prediccionmodelo.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.DatosV5IncompletosException;
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
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import com.backend.nutri_predic.observaciondiaria.service.CompletitudVentanaV5Service;
import com.backend.nutri_predic.observaciondiaria.entity.DominioObservacionDiaria;
import com.backend.nutri_predic.prediccionmodelo.dto.PreparacionAnalisisPredictivoResponse;
import com.backend.nutri_predic.prediccionmodelo.dto.DominioPreparacionAnalisisResponse;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModeloPredictivoService {
    private final ClienteRepository clientes;
    private final ParticipacionEstudioRepository participaciones;
    private final VariablesModeloV5Service variables;
    private final FeatureSchemaV5Mapper featureMapper;
    private final ModeloMlClient clienteMl;
    private final PrediccionModeloRepository predicciones;
    private final CompletitudVentanaV5Service completitud;

    @org.springframework.beans.factory.annotation.Autowired
    public ModeloPredictivoService(
            ClienteRepository clientes,
            ParticipacionEstudioRepository participaciones,
            VariablesModeloV5Service variables,
            FeatureSchemaV5Mapper featureMapper,
            ModeloMlClient clienteMl,
            PrediccionModeloRepository predicciones,
            CompletitudVentanaV5Service completitud) {
        this.clientes = clientes;
        this.participaciones = participaciones;
        this.variables = variables;
        this.featureMapper = featureMapper;
        this.clienteMl = clienteMl;
        this.predicciones = predicciones;
        this.completitud = completitud;
    }

    /** Compatibilidad para pruebas y construcción interna anteriores al endpoint de preparación. */
    public ModeloPredictivoService(
            ClienteRepository clientes,
            ParticipacionEstudioRepository participaciones,
            VariablesModeloV5Service variables,
            FeatureSchemaV5Mapper featureMapper,
            ModeloMlClient clienteMl,
            PrediccionModeloRepository predicciones) {
        this(clientes, participaciones, variables, featureMapper, clienteMl, predicciones, null);
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

        FeatureSchemaV5 schema =
                featureMapper.mapear(variables.construir(cliente.getId(), request.fechaCorte()));
        validarElegibilidadV5(request.fechaCorte(), schema);

        var reutilizable =
                predicciones
                        .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndParticipacionEstudioIdAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                                cliente.getId(), request.fechaCorte(), request.momento(),
                                participacion == null ? null : participacion.getId(),
                                FeatureSchemaV5Mapper.SCHEMA_VERSION, EstadoPrediccionModelo.EXITOSA)
                        .filter(prediccion -> prediccion.getInferenceMs() != null && prediccion.getInferredAt() != null);
        if (reutilizable.isPresent())
            return new ResultadoInferenciaModelo(reutilizable.get(), OrigenResultadoAnalisis.REUTILIZADO);

        trazabilidad.variablesPreparadas().accept(Instant.now());
        trazabilidad.modeloSolicitado().accept(Instant.now());
        try {
            var respuesta = clienteMl.predecir(MlPredictRequest.from(schema));
            trazabilidad.modeloRespondio().accept(Instant.now());

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
            fallo.setSchemaVersion(FeatureSchemaV5Mapper.SCHEMA_VERSION);
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
                .map(PrediccionModeloHistorialResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PreparacionAnalisisPredictivoResponse preparacion(Long clienteId, java.time.LocalDate fechaCorte) {
        if (completitud == null) throw new IllegalStateException("Servicio de completitud V5 no configurado");
        validarFechaCorte(fechaCorte);
        if (!clientes.existsById(clienteId)) throw new ResourceNotFoundException("Cliente");
        FeatureSchemaV5 schema = featureMapper.mapear(variables.construir(clienteId, fechaCorte));
        var faltantes = faltantesV5(fechaCorte, schema);
        var a = completitud.verificar(clienteId, fechaCorte, DominioObservacionDiaria.ALIMENTACION);
        var s = completitud.verificar(clienteId, fechaCorte, DominioObservacionDiaria.SUPLEMENTACION);
        var h = completitud.verificar(clienteId, fechaCorte, DominioObservacionDiaria.HABITOS);
        var dominios = new java.util.LinkedHashMap<String, DominioPreparacionAnalisisResponse>();
        dominios.put("ALIMENTACION", dominio(a)); dominios.put("SUPLEMENTACION", dominio(s)); dominios.put("HABITOS", dominio(h));
        int completos = Math.min(a.diasCompletos(), Math.min(s.diasCompletos(), h.diasCompletos()));
        int disponibles = (int) schema.features().values().stream().filter(java.util.Objects::nonNull).count();
        return new PreparacionAnalisisPredictivoResponse(clienteId, fechaCorte, faltantes.isEmpty(), completos, 7,
                java.util.Map.copyOf(dominios), disponibles, schema.features().size(), java.util.List.copyOf(faltantes));
    }

    private void validarElegibilidadV5(java.time.LocalDate fechaCorte, FeatureSchemaV5 schema) {
        var faltantes = faltantesV5(fechaCorte, schema);
        if (!faltantes.isEmpty()) {
            throw new DatosV5IncompletosException(faltantes);
        }
    }

    private List<String> faltantesV5(java.time.LocalDate fechaCorte, FeatureSchemaV5 schema) {
        var faltantes = new ArrayList<String>();
        schema.features().forEach((nombre, valor) -> { if (valor == null) faltantes.add("X requerida sin valor: " + nombre); });
        if (!faltantes.isEmpty()) faltantes.add(0, "Ventana móvil obligatoria: " + fechaCorte.minusDays(6) + " a " + fechaCorte);
        return faltantes;
    }

    private void validarFechaCorte(java.time.LocalDate fechaCorte) {
        if (fechaCorte == null || fechaCorte.isAfter(java.time.LocalDate.now()))
            throw new BusinessException("fechaCorte no puede ser futura");
    }

    private DominioPreparacionAnalisisResponse dominio(com.backend.nutri_predic.observaciondiaria.dto.CompletitudVentanaV5Response r) {
        return new DominioPreparacionAnalisisResponse(r.ventanaCompleta() ? "COMPLETO" : "INCOMPLETO", r.diasCompletos(), r.diasEsperados());
    }
}
