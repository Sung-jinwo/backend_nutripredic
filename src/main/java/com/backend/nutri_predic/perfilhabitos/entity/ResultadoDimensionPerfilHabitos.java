package com.backend.nutri_predic.perfilhabitos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "resultados_dimension_perfil_habitos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evaluacion_id", "dimension_id"}))
public class ResultadoDimensionPerfilHabitos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluacion_id")
    private EvaluacionPerfilHabitos evaluacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dimension_id")
    private DimensionRubricaPerfilHabitos dimension;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal puntosObtenidos;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal puntosMaximosCalculables;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal coberturaCalculable;

    @Column(length = 2000)
    private String criteriosNoCalculables;

    @Column(length = 2000)
    private String observacion;

    public Long getId() { return id; }
    public EvaluacionPerfilHabitos getEvaluacion() { return evaluacion; }
    public DimensionRubricaPerfilHabitos getDimension() { return dimension; }
    public BigDecimal getPuntosObtenidos() { return puntosObtenidos; }
    public BigDecimal getPuntosMaximosCalculables() { return puntosMaximosCalculables; }
    public BigDecimal getCoberturaCalculable() { return coberturaCalculable; }
    public String getCriteriosNoCalculables() { return criteriosNoCalculables; }
    public String getObservacion() { return observacion; }

    public void setEvaluacion(EvaluacionPerfilHabitos v) { evaluacion = v; }
    public void setDimension(DimensionRubricaPerfilHabitos v) { dimension = v; }
    public void setPuntosObtenidos(BigDecimal v) { puntosObtenidos = v; }
    public void setPuntosMaximosCalculables(BigDecimal v) { puntosMaximosCalculables = v; }
    public void setCoberturaCalculable(BigDecimal v) { coberturaCalculable = v; }
    public void setCriteriosNoCalculables(String v) { criteriosNoCalculables = v; }
    public void setObservacion(String v) { observacion = v; }
}
