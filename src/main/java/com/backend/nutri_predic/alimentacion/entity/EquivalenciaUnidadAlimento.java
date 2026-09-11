package com.backend.nutri_predic.alimentacion.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;

@Entity
@Table(
        name = "equivalencias_unidades_alimentos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"alimento_catalogo_id", "version"}))
public class EquivalenciaUnidadAlimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alimento_catalogo_id")
    private AlimentoCatalogo alimento;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadOrigen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_origen_id")
    private UnidadMedida unidadOrigen;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadDestino;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_destino_id")
    private UnidadMedida unidadDestino;

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

    public BigDecimal getCantidadOrigen() {
        return cantidadOrigen;
    }

    public UnidadMedida getUnidadOrigen() {
        return unidadOrigen;
    }

    public BigDecimal getCantidadDestino() {
        return cantidadDestino;
    }

    public UnidadMedida getUnidadDestino() {
        return unidadDestino;
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

    public void setAlimento(AlimentoCatalogo v) {
        alimento = v;
    }

    public void setVersion(Integer v) {
        version = v;
    }

    public void setCantidadOrigen(BigDecimal v) {
        cantidadOrigen = v;
    }

    public void setUnidadOrigen(UnidadMedida v) {
        unidadOrigen = v;
    }

    public void setCantidadDestino(BigDecimal v) {
        cantidadDestino = v;
    }

    public void setUnidadDestino(UnidadMedida v) {
        unidadDestino = v;
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
