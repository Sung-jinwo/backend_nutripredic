package com.backend.nutri_predic.conocimiento.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
        name = "instrumentos_conocimiento",
        uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "version"}))
public class InstrumentoConocimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInstrumento estado = EstadoInstrumento.BORRADOR;

    private Instant vigenteDesde;

    @Column(length = 1000)
    private String fuenteReferencia;

    @Column(length = 4000)
    private String metadataValidacion;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Integer getVersion() {
        return version;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoInstrumento getEstado() {
        return estado;
    }

    public Instant getVigenteDesde() {
        return vigenteDesde;
    }

    public String getFuenteReferencia() {
        return fuenteReferencia;
    }

    public String getMetadataValidacion() {
        return metadataValidacion;
    }

    public void setCodigo(String v) {
        codigo = v;
    }

    public void setVersion(Integer v) {
        version = v;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setEstado(EstadoInstrumento v) {
        estado = v;
    }

    public void setVigenteDesde(Instant v) {
        vigenteDesde = v;
    }

    public void setFuenteReferencia(String v) {
        fuenteReferencia = v;
    }

    public void setMetadataValidacion(String v) {
        metadataValidacion = v;
    }
}
