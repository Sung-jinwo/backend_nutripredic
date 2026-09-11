package com.backend.nutri_predic.prediccionmodelo.evento.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
        name = "procedimientos_analisis",
        uniqueConstraints = @UniqueConstraint(columnNames = {"codigo", "version"}))
public class ProcedimientoAnalisis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private Integer version;

    @Enumerated(EnumType.STRING)
    private TipoProcedimientoAnalisis tipo;

    private String nombre;
    private String descripcionOperativa;
    private String fuenteReferencia;
    private Instant vigenteDesde;
    private boolean activo = true;
    private String validadoPor;
    private Instant validadoEn;

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

    public TipoProcedimientoAnalisis getTipo() {
        return tipo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setCodigo(String v) {
        codigo = v;
    }

    public void setVersion(Integer v) {
        version = v;
    }

    public void setTipo(TipoProcedimientoAnalisis v) {
        tipo = v;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setDescripcionOperativa(String v) {
        descripcionOperativa = v;
    }

    public void setFuenteReferencia(String v) {
        fuenteReferencia = v;
    }

    public void setVigenteDesde(Instant v) {
        vigenteDesde = v;
    }

    public void setActivo(boolean v) {
        activo = v;
    }

    public void setValidadoPor(String v) {
        validadoPor = v;
    }

    public void setValidadoEn(Instant v) {
        validadoEn = v;
    }
}
