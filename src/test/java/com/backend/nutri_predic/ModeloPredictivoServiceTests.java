package com.backend.nutri_predic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.estudio.entity.EstadoEstudio;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import com.backend.nutri_predic.estudio.repository.ParticipacionEstudioRepository;
import com.backend.nutri_predic.ml.client.ModeloMlClient;
import com.backend.nutri_predic.ml.dto.MlPredictRequest;
import com.backend.nutri_predic.ml.dto.MlPredictResponse;
import com.backend.nutri_predic.ml.dto.MlProbabilidadesResponse;
import com.backend.nutri_predic.ml.exception.ModeloMlException;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.dto.AnalisisPredictivoRequest;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoService;
import com.backend.nutri_predic.prediccionmodelo.service.TrazabilidadInferencia;
import com.backend.nutri_predic.variablemodelov5.dto.VariablesModeloV5Response;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import com.backend.nutri_predic.variablemodelov5.service.VariablesModeloV5Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModeloPredictivoServiceTests {
    private static final Instant INFERRED_AT = Instant.parse("2026-08-30T12:00:00.123Z");

    @Mock ClienteRepository clientes;
    @Mock ParticipacionEstudioRepository participaciones;
    @Mock VariablesModeloV5Service variables;
    @Mock FeatureSchemaV5Mapper mapper;
    @Mock ModeloMlClient ml;
    @Mock PrediccionModeloRepository predicciones;
    @Mock Cliente cliente;

    private ModeloPredictivoService service;
    private final LocalDate corte = LocalDate.of(2026, 8, 20);

    @BeforeEach
    void setUp() {
        service =
                new ModeloPredictivoService(
                        clientes, participaciones, variables, mapper, ml, predicciones);
        when(cliente.getId()).thenReturn(1L);
        when(clientes.findById(1L)).thenReturn(Optional.of(cliente));
    }

    @Test
    void persistePrediccionExitosaConContratoV5YMetadataPython() {
        prepararSchema(schemaConInformacion());
        sinReutilizable(MomentoEvaluacion.BASAL);
        when(ml.predecir(any())).thenReturn(respuesta());
        when(predicciones.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var resultado =
                service.predecirConTrazabilidad(
                        request(MomentoEvaluacion.BASAL), TrazabilidadInferencia.sinRegistro());

        assertThat(resultado.origenResultado()).isEqualTo(OrigenResultadoAnalisis.GENERADO);
        verify(variables).construir(1L, corte);
        var solicitud = ArgumentCaptor.forClass(MlPredictRequest.class);
        verify(ml).predecir(solicitud.capture());
        assertThat(solicitud.getValue().schemaVersion())
                .isEqualTo(FeatureSchemaV5Mapper.SCHEMA_VERSION);
        assertThat(solicitud.getValue().features())
                .hasSize(21)
                .containsEntry("edad", 30)
                .containsEntry("tipo_objetivo_fisico", "GANAR_MASA")
                .doesNotContainKeys("PCC", "PCS", "TPP", "nivelConocimiento", "nivelConsumo");

        var guardada = ArgumentCaptor.forClass(PrediccionModelo.class);
        verify(predicciones).saveAndFlush(guardada.capture());
        assertThat(guardada.getValue().getEstado()).isEqualTo(EstadoPrediccionModelo.EXITOSA);
        assertThat(guardada.getValue().getSchemaVersion())
                .isEqualTo(FeatureSchemaV5Mapper.SCHEMA_VERSION);
        assertThat(guardada.getValue().getInferenceMs()).isEqualByComparingTo("12.6");
        assertThat(guardada.getValue().getTiempoInferenciaMs()).isNull();
        assertThat(guardada.getValue().getInferredAt()).isEqualTo(INFERRED_AT);
    }

    @Test
    void falloMlNoFabricaMetadataDeInferencia() {
        prepararSchema(schemaConInformacion());
        sinReutilizable(MomentoEvaluacion.BASAL);
        when(ml.predecir(any())).thenThrow(new ModeloMlException("detalle interno"));
        when(predicciones.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(
                        () ->
                                service.predecirConTrazabilidad(
                                        request(MomentoEvaluacion.BASAL),
                                        TrazabilidadInferencia.sinRegistro()))
                .isInstanceOf(ModeloMlException.class);

        var guardada = ArgumentCaptor.forClass(PrediccionModelo.class);
        verify(predicciones).saveAndFlush(guardada.capture());
        assertThat(guardada.getValue().getEstado()).isEqualTo(EstadoPrediccionModelo.FALLIDA);
        assertThat(guardada.getValue().getClasificacionPredicha()).isNull();
        assertThat(guardada.getValue().getInferenceMs()).isNull();
        assertThat(guardada.getValue().getTiempoInferenciaMs()).isNull();
        assertThat(guardada.getValue().getInferredAt()).isNull();
    }

    @Test
    void reutilizaSoloResultadoV5SinNuevaLlamadaMl() {
        var existente = new PrediccionModelo();
        existente.setCliente(cliente);
        existente.setFechaCorte(corte);
        existente.setMomentoEvaluacion(MomentoEvaluacion.FINAL);
        existente.setSchemaVersion(FeatureSchemaV5Mapper.SCHEMA_VERSION);
        existente.setModelVersion("modelo-v5");
        existente.setClasificacionPredicha(ClasificacionPerfilHabitos.ADECUADO);
        existente.setProbAdecuado(new BigDecimal("0.80"));
        existente.setProbMejorable(new BigDecimal("0.15"));
        existente.setProbCritico(new BigDecimal("0.05"));
        existente.setInferenceMs(new BigDecimal("4.25"));
        existente.setInferredAt(INFERRED_AT);
        existente.setEstado(EstadoPrediccionModelo.EXITOSA);
        when(predicciones
                        .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndParticipacionEstudioIdAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                                1L,
                                corte,
                                MomentoEvaluacion.FINAL,
                                null,
                                FeatureSchemaV5Mapper.SCHEMA_VERSION,
                                EstadoPrediccionModelo.EXITOSA))
                .thenReturn(Optional.of(existente));

        var resultado =
                service.predecirConTrazabilidad(
                        request(MomentoEvaluacion.FINAL), TrazabilidadInferencia.sinRegistro());

        assertThat(resultado.prediccion()).isSameAs(existente);
        assertThat(resultado.origenResultado()).isEqualTo(OrigenResultadoAnalisis.REUTILIZADO);
        verifyNoInteractions(variables, mapper, ml);
        verify(predicciones, never()).saveAndFlush(any());
    }

    @Test
    void rechazaVectorCompletamenteVacioSinLlamarAlModelo() {
        prepararSchema(schemaVacio());
        sinReutilizable(MomentoEvaluacion.BASAL);
        @SuppressWarnings("unchecked")
        Consumer<Instant> modeloRespondio = mock(Consumer.class);
        TrazabilidadInferencia trazabilidad =
                new TrazabilidadInferencia(instante -> {}, instante -> {}, modeloRespondio);

        assertThatThrownBy(
                        () ->
                                service.predecirConTrazabilidad(
                                        request(MomentoEvaluacion.BASAL), trazabilidad))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(ml, modeloRespondio);
        verify(predicciones, never()).saveAndFlush(any());
    }

    @Test
    void validaParticipacionActivaYDelMismoCliente() {
        var otroCliente = mock(Cliente.class);
        when(otroCliente.getId()).thenReturn(2L);
        var participacion = new ParticipacionEstudio();
        participacion.setCliente(otroCliente);
        participacion.setEstado(EstadoEstudio.ACTIVO);
        when(participaciones.findById(44L)).thenReturn(Optional.of(participacion));

        assertThatThrownBy(
                        () ->
                                service.predecirConTrazabilidad(
                                        new AnalisisPredictivoRequest(
                                                1L, corte, 44L, MomentoEvaluacion.BASAL),
                                        TrazabilidadInferencia.sinRegistro()))
                .isInstanceOf(BusinessException.class);

        verifyNoInteractions(variables, mapper, ml, predicciones);
    }

    private void prepararSchema(FeatureSchemaV5 schema) {
        when(variables.construir(1L, corte)).thenReturn(mock(VariablesModeloV5Response.class));
        when(mapper.mapear(any(VariablesModeloV5Response.class))).thenReturn(schema);
    }

    private void sinReutilizable(MomentoEvaluacion momento) {
        when(predicciones
                        .findFirstByClienteIdAndFechaCorteAndMomentoEvaluacionAndParticipacionEstudioIdAndSchemaVersionAndEstadoOrderByFechaPrediccionDesc(
                                1L,
                                corte,
                                momento,
                                null,
                                FeatureSchemaV5Mapper.SCHEMA_VERSION,
                                EstadoPrediccionModelo.EXITOSA))
                .thenReturn(Optional.empty());
    }

    private AnalisisPredictivoRequest request(MomentoEvaluacion momento) {
        return new AnalisisPredictivoRequest(1L, corte, null, momento);
    }

    private MlPredictResponse respuesta() {
        return new MlPredictResponse(
                "ADECUADO",
                new MlProbabilidadesResponse(
                        new BigDecimal("0.80"), new BigDecimal("0.15"), new BigDecimal("0.05")),
                "modelo-v5",
                FeatureSchemaV5Mapper.SCHEMA_VERSION,
                new BigDecimal("12.6"),
                INFERRED_AT);
    }

    private FeatureSchemaV5 schemaConInformacion() {
        var features = baseFeatures();
        features.put("edad", 30);
        features.put("peso_kg", new BigDecimal("70.0"));
        features.put("altura_cm", new BigDecimal("170.0"));
        features.put("tipo_objetivo_fisico", "GANAR_MASA");
        features.put("promedio_kcal_7d", new BigDecimal("2100"));
        return new FeatureSchemaV5(FeatureSchemaV5Mapper.SCHEMA_VERSION, features);
    }

    private FeatureSchemaV5 schemaVacio() {
        return new FeatureSchemaV5(FeatureSchemaV5Mapper.SCHEMA_VERSION, baseFeatures());
    }

    private LinkedHashMap<String, Object> baseFeatures() {
        var features = new LinkedHashMap<String, Object>();
        FeatureSchemaV5Mapper.FEATURE_NAMES.forEach(nombre -> features.put(nombre, null));
        return features;
    }
}
