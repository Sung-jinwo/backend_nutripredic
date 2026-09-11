package com.backend.nutri_predic.perfilhabitos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(
        name = "dimensiones_rubrica_perfil_habitos",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_dimension_perfil_rubrica_codigo",
                    columnNames = {"rubrica_id", "codigo"}),
            @UniqueConstraint(
                    name = "uq_dimension_perfil_rubrica_orden",
                    columnNames = {"rubrica_id", "orden"})
        })
public class DimensionRubricaPerfilHabitos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rubrica_id")
    private RubricaPerfilHabitos rubrica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private CodigoDimensionPerfilHabitos codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    @Column(precision = 6, scale = 2)
    private BigDecimal puntajeMinimo;

    @Column(precision = 6, scale = 2)
    private BigDecimal puntajeMaximo;

    @Column(precision = 6, scale = 2)
    private BigDecimal pesoPorcentual;

    @Column(nullable = false)
    private Integer orden;

    public Long getId() {
        return id;
    }

    public RubricaPerfilHabitos getRubrica() {
        return rubrica;
    }

    public CodigoDimensionPerfilHabitos getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPuntajeMinimo() {
        return puntajeMinimo;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public BigDecimal getPesoPorcentual() {
        return pesoPorcentual;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setRubrica(RubricaPerfilHabitos rubrica) {
        this.rubrica = rubrica;
    }

    public void setCodigo(CodigoDimensionPerfilHabitos codigo) {
        this.codigo = codigo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPuntajeMinimo(BigDecimal puntajeMinimo) {
        this.puntajeMinimo = puntajeMinimo;
    }

    public void setPuntajeMaximo(BigDecimal puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    public void setPesoPorcentual(BigDecimal pesoPorcentual) {
        this.pesoPorcentual = pesoPorcentual;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }
}
