package com.backend.nutri_predic.consumo.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "evaluaciones_consumo")
public class EvaluacionConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participacion_estudio_id")
    private ParticipacionEstudio participacionEstudio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prediccion_modelo_id")
    private PrediccionModelo prediccionModelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_consumo_id")
    private RubricaConsumoSuplementos rubrica;

    @OneToOne(mappedBy = "evaluacion", fetch = FetchType.LAZY)
    private SnapshotEvaluacionConsumo resultadoFactual;

    @Column(name = "fecha_corte", nullable = false)
    private LocalDate fechaCorte;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "ventana_dias", nullable = false)
    private Integer ventanaDias;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentoEvaluacion momento = MomentoEvaluacion.NO_DETERMINADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_clasificacion", nullable = false)
    private EstadoClasificacionConsumo estadoClasificacion =
            EstadoClasificacionConsumo.NO_DETERMINADA;

    @Column(name = "motivo_clasificacion", length = 100)
    private String motivoClasificacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validez", nullable = false)
    private EstadoValidezMedicion estadoValidez = EstadoValidezMedicion.NO_DETERMINADA;

    @Column(name = "motivo_invalidez", length = 1000)
    private String motivoInvalidez;

    @Column(name = "alto_consumo")
    private Boolean altoConsumo;

    @Column(name = "fecha_evaluacion", nullable = false)
    private Instant fechaEvaluacion = Instant.now();

    @Column(name = "schema_version", nullable = false, length = 80)
    private String schemaVersion;

    @Column(length = 4000)
    private String advertencias;

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public RubricaConsumoSuplementos getRubrica() {
        return rubrica;
    }
    public PrediccionModelo getPrediccionModelo() { return prediccionModelo; }

    public SnapshotEvaluacionConsumo getResultadoFactual() {
        return resultadoFactual;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public Integer getVentanaDias() {
        return ventanaDias;
    }

    public MomentoEvaluacion getMomento() {
        return momento;
    }

    public EstadoClasificacionConsumo getEstadoClasificacion() {
        return estadoClasificacion;
    }

    public String getMotivoClasificacion() {
        return motivoClasificacion;
    }

    public EstadoValidezMedicion getEstadoValidez() {
        return estadoValidez;
    }

    public Boolean getAltoConsumo() {
        return altoConsumo;
    }

    public Instant getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public String getAdvertencias() {
        return advertencias;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public void setParticipacionEstudio(ParticipacionEstudio v) {
        participacionEstudio = v;
    }
    public void setPrediccionModelo(PrediccionModelo v) { prediccionModelo = v; }

    public void setRubrica(RubricaConsumoSuplementos v) {
        rubrica = v;
    }

    public void setFechaCorte(LocalDate v) {
        fechaCorte = v;
    }

    public void setFechaInicio(LocalDate v) {
        fechaInicio = v;
    }

    public void setVentanaDias(Integer v) {
        ventanaDias = v;
    }

    public void setMomento(MomentoEvaluacion v) {
        momento = v;
    }

    public void setEstadoClasificacion(EstadoClasificacionConsumo v) {
        estadoClasificacion = v;
    }

    public void setMotivoClasificacion(String v) {
        motivoClasificacion = v;
    }

    public void setEstadoValidez(EstadoValidezMedicion v) {
        estadoValidez = v;
    }

    public void setMotivoInvalidez(String v) {
        motivoInvalidez = v;
    }

    public void setAltoConsumo(Boolean v) {
        altoConsumo = v;
    }

    public void setSchemaVersion(String v) {
        schemaVersion = v;
    }

    public void setAdvertencias(String v) {
        advertencias = v;
    }
}
