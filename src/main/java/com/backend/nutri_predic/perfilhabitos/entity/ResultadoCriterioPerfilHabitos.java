package com.backend.nutri_predic.perfilhabitos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "resultados_criterio_perfil_habitos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"evaluacion_id", "criterio_id"}))
public class ResultadoCriterioPerfilHabitos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluacion_id")
    private EvaluacionPerfilHabitos evaluacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criterio_id")
    private CriterioRubricaPerfilHabitos criterio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private EstadoResultadoCriterioPerfil estado;

    @Column(precision = 12, scale = 4)
    private BigDecimal valorObservado;

    @Column(precision = 6, scale = 2)
    private BigDecimal puntosObtenidos;

    @Column(precision = 6, scale = 2)
    private BigDecimal puntosMaximos;

    @Column(length = 1000)
    private String referenciaAplicada;

    @Column(length = 1000)
    private String motivoNoCalculable;

    @Column(length = 2000)
    private String observacion;

    public Long getId() { return id; }
    public EvaluacionPerfilHabitos getEvaluacion() { return evaluacion; }
    public CriterioRubricaPerfilHabitos getCriterio() { return criterio; }
    public EstadoResultadoCriterioPerfil getEstado() { return estado; }
    public BigDecimal getValorObservado() { return valorObservado; }
    public BigDecimal getPuntosObtenidos() { return puntosObtenidos; }
    public BigDecimal getPuntosMaximos() { return puntosMaximos; }
    public String getReferenciaAplicada() { return referenciaAplicada; }
    public String getMotivoNoCalculable() { return motivoNoCalculable; }
    public String getObservacion() { return observacion; }

    public void setEvaluacion(EvaluacionPerfilHabitos v) { evaluacion = v; }
    public void setCriterio(CriterioRubricaPerfilHabitos v) { criterio = v; }
    public void setEstado(EstadoResultadoCriterioPerfil v) { estado = v; }
    public void setValorObservado(BigDecimal v) { valorObservado = v; }
    public void setPuntosObtenidos(BigDecimal v) { puntosObtenidos = v; }
    public void setPuntosMaximos(BigDecimal v) { puntosMaximos = v; }
    public void setReferenciaAplicada(String v) { referenciaAplicada = v; }
    public void setMotivoNoCalculable(String v) { motivoNoCalculable = v; }
    public void setObservacion(String v) { observacion = v; }
}
