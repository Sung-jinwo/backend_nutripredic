package com.backend.nutri_predic.alimentacion.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "composiciones_nutricionales_alimentos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"alimento_catalogo_id", "version"}))
public class ComposicionNutricionalAlimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alimento_catalogo_id")
    private AlimentoCatalogo alimento;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadReferencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_referencia_id")
    private UnidadMedida unidadReferencia;

    @Column(precision = 12, scale = 4)
    private BigDecimal kcal;

    @Column(name = "proteina_g", precision = 12, scale = 4)
    private BigDecimal proteinaG;

    @Column(name = "carbohidratos_g", precision = 12, scale = 4)
    private BigDecimal carbohidratosG;

    @Column(name = "grasas_g", precision = 12, scale = 4)
    private BigDecimal grasasG;

    @Column(name = "fibra_g", precision = 12, scale = 4)
    private BigDecimal fibraG;

    @Column(name = "azucar_g", precision = 12, scale = 4)
    private BigDecimal azucarG;

    @Column(name = "sodio_mg", precision = 12, scale = 4)
    private BigDecimal sodioMg;

    @Column(name = "fuente_datos", length = 1000)
    private String fuenteDatos;

    @Column(nullable = false)
    private LocalDate fechaDesde;

    private LocalDate fechaHasta;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public AlimentoCatalogo getAlimento() {
        return alimento;
    }

    public Integer getVersion() {
        return version;
    }

    public BigDecimal getCantidadReferencia() {
        return cantidadReferencia;
    }

    public UnidadMedida getUnidadReferencia() {
        return unidadReferencia;
    }

    public BigDecimal getKcal() {
        return kcal;
    }

    public BigDecimal getProteinaG() {
        return proteinaG;
    }

    public BigDecimal getCarbohidratosG() {
        return carbohidratosG;
    }

    public BigDecimal getGrasasG() {
        return grasasG;
    }

    public BigDecimal getFibraG() {
        return fibraG;
    }

    public BigDecimal getAzucarG() {
        return azucarG;
    }

    public BigDecimal getSodioMg() {
        return sodioMg;
    }

    public String getFuenteDatos() {
        return fuenteDatos;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setAlimento(AlimentoCatalogo v) {
        alimento = v;
    }

    public void setVersion(Integer v) {
        version = v;
    }

    public void setCantidadReferencia(BigDecimal v) {
        cantidadReferencia = v;
    }

    public void setUnidadReferencia(UnidadMedida v) {
        unidadReferencia = v;
    }

    public void setKcal(BigDecimal v) {
        kcal = v;
    }

    public void setProteinaG(BigDecimal v) {
        proteinaG = v;
    }

    public void setCarbohidratosG(BigDecimal v) {
        carbohidratosG = v;
    }

    public void setGrasasG(BigDecimal v) {
        grasasG = v;
    }

    public void setFibraG(BigDecimal v) {
        fibraG = v;
    }

    public void setAzucarG(BigDecimal v) {
        azucarG = v;
    }

    public void setSodioMg(BigDecimal v) {
        sodioMg = v;
    }

    public void setFuenteDatos(String v) {
        fuenteDatos = v;
    }

    public void setFechaDesde(LocalDate v) {
        fechaDesde = v;
    }

    public void setFechaHasta(LocalDate v) {
        fechaHasta = v;
    }

    public void setActivo(boolean v) {
        activo = v;
    }
}
