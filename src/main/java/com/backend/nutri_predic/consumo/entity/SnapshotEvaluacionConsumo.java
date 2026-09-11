package com.backend.nutri_predic.consumo.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "snapshots_evaluacion_consumo")
public class SnapshotEvaluacionConsumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "evaluacion_consumo_id")
    private EvaluacionConsumo evaluacion;

    private String schemaVersion;

    @Column(name = "contenido_json", columnDefinition = "text")
    private String contenidoJson;

    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public EvaluacionConsumo getEvaluacion() {
        return evaluacion;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getContenidoJson() {
        return contenidoJson;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setEvaluacion(EvaluacionConsumo v) {
        evaluacion = v;
    }

    public void setSchemaVersion(String v) {
        schemaVersion = v;
    }

    public void setContenidoJson(String v) {
        contenidoJson = v;
    }
}
