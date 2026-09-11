package com.backend.nutri_predic.consumo.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "criterios_consumo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "version"}))
public class RubricaConsumoSuplementos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoCriterioConsumo estado = EstadoCriterioConsumo.BORRADOR;

    @Column(name = "ventana_dias")
    private Integer ventanaDias;

    @Column(name = "vigente_desde")
    private Instant vigenteDesde;

    @Column(name = "vigente_hasta")
    private Instant vigenteHasta;

    @Column(nullable = false)
    private boolean validada;

    @Column(length = 2000)
    private String observacion;

    @Column(name = "fuente_referencia", length = 1000)
    private String fuenteReferencia;

    @Column(name = "validado_por")
    private String validadoPor;

    @Column(name = "validado_en")
    private Instant validadoEn;

    @Column(name = "version_evaluador", length = 80)
    private String versionEvaluador;

    @Column(name = "metadata_validacion", length = 4000)
    private String metadataValidacion;

    @Column(name = "regla_global", length = 4000)
    private String reglaGlobal;

    @OneToMany(
            mappedBy = "rubrica",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<CriterioConsumoSuplementos> criterios = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Integer getVersion() {
        return version;
    }

    public EstadoCriterioConsumo getEstado() {
        return estado;
    }

    public Integer getVentanaDias() {
        return ventanaDias;
    }

    public Instant getVigenteDesde() {
        return vigenteDesde;
    }

    public Instant getVigenteHasta() {
        return vigenteHasta;
    }

    public boolean isValidada() {
        return validada;
    }

    public String getObservacion() {
        return observacion;
    }

    public String getFuenteReferencia() {
        return fuenteReferencia;
    }

    public String getValidadoPor() {
        return validadoPor;
    }

    public Instant getValidadoEn() {
        return validadoEn;
    }

    public String getVersionEvaluador() {
        return versionEvaluador;
    }

    public String getMetadataValidacion() {
        return metadataValidacion;
    }

    public String getReglaGlobal() {
        return reglaGlobal;
    }

    public List<CriterioConsumoSuplementos> getCriterios() {
        return criterios;
    }

    public void setCodigo(String valor) {
        codigo = valor;
    }

    public void setVersion(Integer valor) {
        version = valor;
    }

    public void setEstado(EstadoCriterioConsumo valor) {
        estado = valor;
    }

    public void setVentanaDias(Integer valor) {
        ventanaDias = valor;
    }

    public void setVigenteDesde(Instant valor) {
        vigenteDesde = valor;
    }

    public void setVigenteHasta(Instant valor) {
        vigenteHasta = valor;
    }

    public void setValidada(boolean valor) {
        validada = valor;
    }

    public void setObservacion(String valor) {
        observacion = valor;
    }

    public void setFuenteReferencia(String valor) {
        fuenteReferencia = valor;
    }

    public void setValidadoPor(String valor) {
        validadoPor = valor;
    }

    public void setValidadoEn(Instant valor) {
        validadoEn = valor;
    }

    public void setVersionEvaluador(String valor) {
        versionEvaluador = valor;
    }

    public void setMetadataValidacion(String valor) {
        metadataValidacion = valor;
    }

    public void setReglaGlobal(String valor) {
        reglaGlobal = valor;
    }
}
