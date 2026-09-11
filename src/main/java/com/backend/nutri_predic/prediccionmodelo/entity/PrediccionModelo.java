package com.backend.nutri_predic.prediccionmodelo.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "predicciones_modelo")
public class PrediccionModelo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "participacion_estudio_id")
    private ParticipacionEstudio participacionEstudio;

    @Enumerated(EnumType.STRING)
    @Column(name = "momento_evaluacion", nullable = false)
    private MomentoEvaluacion momentoEvaluacion = MomentoEvaluacion.NO_DETERMINADO;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Enumerated(EnumType.STRING)
    @Column(name = "clasificacion_predicha")
    private ClasificacionPerfilHabitos clasificacionPredicha;

    @Column(name = "prob_adecuado", precision = 8, scale = 6)
    private BigDecimal probAdecuado;

    @Column(name = "prob_mejorable", precision = 8, scale = 6)
    private BigDecimal probMejorable;

    @Column(name = "prob_critico", precision = 8, scale = 6)
    private BigDecimal probCritico;

    @Column(name = "model_version", length = 255)
    private String modelVersion;

    @Column(name = "schema_version", nullable = false, length = 80)
    private String schemaVersion;

    @Column(name = "fecha_prediccion", nullable = false)
    private Instant fechaPrediccion = Instant.now();

    @Column(name = "tiempo_inferencia_ms")
    private Long tiempoInferenciaMs;

    @Column(name = "inference_ms", precision = 14, scale = 6)
    private BigDecimal inferenceMs;

    @Column(name = "inferred_at")
    private Instant inferredAt;

    @Column(name = "model_type", length = 80)
    private String modelType;

    @Column(name = "training_data_type", length = 80)
    private String trainingDataType;

    @Column(name = "is_thesis_final_model")
    private Boolean thesisFinalModel;

    @Column(name = "feature_count")
    private Integer featureCount;

    @Column(name = "meta_kcal", precision = 19, scale = 2)
    private BigDecimal metaKcal;
    @Column(name = "meta_proteina_g", precision = 19, scale = 2)
    private BigDecimal metaProteinaG;
    @Column(name = "meta_carbohidratos_g", precision = 19, scale = 2)
    private BigDecimal metaCarbohidratosG;
    @Column(name = "meta_grasas_g", precision = 19, scale = 2)
    private BigDecimal metaGrasasG;
    @Column(name = "meta_agua_ml", precision = 19, scale = 2)
    private BigDecimal metaAguaMl;
    @Column(name = "formula_nutricional_version", length = 100)
    private String formulaNutricionalVersion;
    @Column(name = "fuente_formula_nutricional", length = 500)
    private String fuenteFormulaNutricional;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrediccionModelo estado;

    @Column(name = "mensaje_error", length = 2000)
    private String mensajeError;

    @Column(name = "observaciones_tecnicas", length = 2000)
    private String observacionesTecnicas;

    @PrePersist
    @PreUpdate
    private void validarEstado() {
        if (cliente == null
                || fechaCorte == null
                || fechaPrediccion == null
                || estado == null
                || vacio(schemaVersion))
            throw new IllegalStateException(
                    "La predicción de modelo requiere contexto y schemaVersion");
        if (estado == EstadoPrediccionModelo.EXITOSA
                && (vacio(modelVersion)
                        || clasificacionPredicha == null
                        || probAdecuado == null
                        || probMejorable == null
                        || probCritico == null))
            throw new IllegalStateException(
                    "Una predicción exitosa requiere clasificación, probabilidades y modelVersion");
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public ParticipacionEstudio getParticipacionEstudio() {
        return participacionEstudio;
    }

    public MomentoEvaluacion getMomentoEvaluacion() {
        return momentoEvaluacion;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public ClasificacionPerfilHabitos getClasificacionPredicha() {
        return clasificacionPredicha;
    }

    public BigDecimal getProbAdecuado() {
        return probAdecuado;
    }

    public BigDecimal getProbMejorable() {
        return probMejorable;
    }

    public BigDecimal getProbCritico() {
        return probCritico;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public Instant getFechaPrediccion() {
        return fechaPrediccion;
    }

    public Long getTiempoInferenciaMs() {
        return tiempoInferenciaMs;
    }

    public BigDecimal getInferenceMs() {
        return inferenceMs;
    }

    public Instant getInferredAt() {
        return inferredAt;
    }

    public String getModelType() { return modelType; }

    public String getTrainingDataType() { return trainingDataType; }

    public Boolean getThesisFinalModel() { return thesisFinalModel; }

    public Integer getFeatureCount() { return featureCount; }
    public BigDecimal getMetaKcal() { return metaKcal; }
    public BigDecimal getMetaProteinaG() { return metaProteinaG; }
    public BigDecimal getMetaCarbohidratosG() { return metaCarbohidratosG; }
    public BigDecimal getMetaGrasasG() { return metaGrasasG; }
    public BigDecimal getMetaAguaMl() { return metaAguaMl; }
    public String getFormulaNutricionalVersion() { return formulaNutricionalVersion; }
    public String getFuenteFormulaNutricional() { return fuenteFormulaNutricional; }

    public EstadoPrediccionModelo getEstado() {
        return estado;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public String getObservacionesTecnicas() {
        return observacionesTecnicas;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public void setParticipacionEstudio(ParticipacionEstudio v) {
        participacionEstudio = v;
    }

    public void setMomentoEvaluacion(MomentoEvaluacion v) {
        momentoEvaluacion = v;
    }

    public void setFechaCorte(LocalDate v) {
        fechaCorte = v;
    }

    public void setClasificacionPredicha(ClasificacionPerfilHabitos v) {
        clasificacionPredicha = v;
    }

    public void setProbAdecuado(BigDecimal v) {
        probAdecuado = v;
    }

    public void setProbMejorable(BigDecimal v) {
        probMejorable = v;
    }

    public void setProbCritico(BigDecimal v) {
        probCritico = v;
    }

    public void setModelVersion(String v) {
        modelVersion = v;
    }

    public void setSchemaVersion(String v) {
        schemaVersion = v;
    }

    public void setFechaPrediccion(Instant v) {
        fechaPrediccion = v;
    }

    public void setTiempoInferenciaMs(Long v) {
        tiempoInferenciaMs = v;
    }

    public void setInferenceMs(BigDecimal v) {
        inferenceMs = v;
    }

    public void setInferredAt(Instant v) {
        inferredAt = v;
    }

    public void setModelType(String v) { modelType = v; }

    public void setTrainingDataType(String v) { trainingDataType = v; }

    public void setThesisFinalModel(Boolean v) { thesisFinalModel = v; }

    public void setFeatureCount(Integer v) { featureCount = v; }
    public void setMetaKcal(BigDecimal v) { metaKcal = v; }
    public void setMetaProteinaG(BigDecimal v) { metaProteinaG = v; }
    public void setMetaCarbohidratosG(BigDecimal v) { metaCarbohidratosG = v; }
    public void setMetaGrasasG(BigDecimal v) { metaGrasasG = v; }
    public void setMetaAguaMl(BigDecimal v) { metaAguaMl = v; }
    public void setFormulaNutricionalVersion(String v) { formulaNutricionalVersion = v; }
    public void setFuenteFormulaNutricional(String v) { fuenteFormulaNutricional = v; }

    public void setEstado(EstadoPrediccionModelo v) {
        estado = v;
    }

    public void setMensajeError(String v) {
        mensajeError = v;
    }

    public void setObservacionesTecnicas(String v) {
        observacionesTecnicas = v;
    }
}
