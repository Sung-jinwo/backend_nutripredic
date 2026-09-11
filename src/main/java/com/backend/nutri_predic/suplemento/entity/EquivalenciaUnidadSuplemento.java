package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equivalencias_unidades_suplementos")
public class EquivalenciaUnidadSuplemento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "suplemento_catalogo_id")
    private SuplementoCatalogo suplemento;

    private Integer version;
    private BigDecimal cantidadOrigen;

    @ManyToOne
    @JoinColumn(name = "unidad_origen_id")
    private UnidadMedida unidadOrigen;

    private BigDecimal cantidadDestino;

    @ManyToOne
    @JoinColumn(name = "unidad_destino_id")
    private UnidadMedida unidadDestino;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Boolean activo;

    public Long getId() {
        return id;
    }

    public SuplementoCatalogo getSuplemento() {
        return suplemento;
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

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setSuplemento(SuplementoCatalogo v) {
        suplemento = v;
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

    public void setFechaDesde(LocalDate v) {
        fechaDesde = v;
    }

    public void setFechaHasta(LocalDate v) {
        fechaHasta = v;
    }

    public void setActivo(Boolean v) {
        activo = v;
    }
}
