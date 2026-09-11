package com.backend.nutri_predic.perfilhabitos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "rubricas_perfil_habitos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "version"}))
public class RubricaPerfilHabitos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal version;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRubricaPerfilHabitos estado = EstadoRubricaPerfilHabitos.BORRADOR;

    private Instant vigenteDesde;
    private Instant vigenteHasta;
    private String validadoPor;
    private Instant validadoEn;

    @Column(length = 2000)
    private String observacionValidacion;

    @Column(length = 80)
    private String tipoValidacion;

    @Column(length = 4000)
    private String fuenteValidacion;

    @Column(length = 80)
    private String versionFuente;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public BigDecimal getVersion() {
        return version;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoRubricaPerfilHabitos getEstado() {
        return estado;
    }

    public Instant getVigenteDesde() {
        return vigenteDesde;
    }

    public String getValidadoPor() {
        return validadoPor;
    }

    public Instant getValidadoEn() {
        return validadoEn;
    }

    public Instant getVigenteHasta() {
        return vigenteHasta;
    }

    public String getObservacionValidacion() {
        return observacionValidacion;
    }

    public String getTipoValidacion() {
        return tipoValidacion;
    }

    public String getFuenteValidacion() {
        return fuenteValidacion;
    }

    public String getVersionFuente() {
        return versionFuente;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCodigo(String v) {
        codigo = v;
    }

    public void setVersion(BigDecimal v) {
        version = v;
    }

    public void setVersion(Integer v) {
        version = v == null ? null : BigDecimal.valueOf(v.longValue());
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setDescripcion(String v) {
        descripcion = v;
    }

    public void setEstado(EstadoRubricaPerfilHabitos v) {
        estado = v;
    }

    public void setVigenteDesde(Instant v) {
        vigenteDesde = v;
    }

    public void setVigenteHasta(Instant v) {
        vigenteHasta = v;
    }

    public void setValidadoPor(String v) {
        validadoPor = v;
    }

    public void setValidadoEn(Instant v) {
        validadoEn = v;
    }

    public void setObservacionValidacion(String v) {
        observacionValidacion = v;
    }

    public void setTipoValidacion(String v) {
        tipoValidacion = v;
    }

    public void setFuenteValidacion(String v) {
        fuenteValidacion = v;
    }

    public void setVersionFuente(String v) {
        versionFuente = v;
    }
}
