package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;

@Entity
@Table(name = "composiciones_suplementos")
public class ComposicionSuplemento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "suplemento_catalogo_id")
    private SuplementoCatalogo suplemento;

    private Integer version;

    @Column(name = "cantidad_porcion_referencia", precision = 12, scale = 4)
    private BigDecimal cantidadPorcionReferencia;

    @Column(name = "energia_kcal_porcion", precision = 10, scale = 2)
    private BigDecimal energiaKcalPorcion;

    @ManyToOne
    @JoinColumn(name = "unidad_porcion_id")
    private UnidadMedida unidadPorcion;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Boolean activo;
    private String fuenteDatos;

    public Long getId() {
        return id;
    }

    public SuplementoCatalogo getSuplemento() {
        return suplemento;
    }

    public Integer getVersion() {
        return version;
    }

    public BigDecimal getCantidadPorcionReferencia() {
        return cantidadPorcionReferencia;
    }
    public BigDecimal getEnergiaKcalPorcion() { return energiaKcalPorcion; }

    public UnidadMedida getUnidadPorcion() {
        return unidadPorcion;
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

    public String getFuenteDatos() {
        return fuenteDatos;
    }

    public void setSuplemento(SuplementoCatalogo x) {
        suplemento = x;
    }

    public void setVersion(Integer x) {
        version = x;
    }

    public void setCantidadPorcionReferencia(BigDecimal x) {
        cantidadPorcionReferencia = x;
    }
    public void setEnergiaKcalPorcion(BigDecimal x) { energiaKcalPorcion = x; }

    public void setUnidadPorcion(UnidadMedida x) {
        unidadPorcion = x;
    }

    public void setFechaDesde(LocalDate x) {
        fechaDesde = x;
    }

    public void setFechaHasta(LocalDate x) {
        fechaHasta = x;
    }

    public void setActivo(Boolean x) {
        activo = x;
    }

    public void setFuenteDatos(String x) {
        fuenteDatos = x;
    }
}
