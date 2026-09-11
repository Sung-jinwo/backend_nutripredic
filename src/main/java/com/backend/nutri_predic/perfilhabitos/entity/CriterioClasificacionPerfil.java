package com.backend.nutri_predic.perfilhabitos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "criterios_clasificacion_perfil",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"rubrica_id", "clasificacion"}),
            @UniqueConstraint(columnNames = {"rubrica_id", "orden"})
        })
public class CriterioClasificacionPerfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rubrica_id")
    private RubricaPerfilHabitos rubrica;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClasificacionPerfilHabitos clasificacion;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal limiteInferior;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal limiteSuperior;

    @Column(nullable = false)
    private boolean incluyeInferior;

    @Column(nullable = false)
    private boolean incluyeSuperior;

    @Column(nullable = false)
    private Integer orden;

    public Long getId() {
        return id;
    }

    public RubricaPerfilHabitos getRubrica() {
        return rubrica;
    }

    public ClasificacionPerfilHabitos getClasificacion() {
        return clasificacion;
    }

    public BigDecimal getLimiteInferior() {
        return limiteInferior;
    }

    public BigDecimal getLimiteSuperior() {
        return limiteSuperior;
    }

    public boolean isIncluyeInferior() {
        return incluyeInferior;
    }

    public boolean isIncluyeSuperior() {
        return incluyeSuperior;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setRubrica(RubricaPerfilHabitos v) {
        rubrica = v;
    }

    public void setClasificacion(ClasificacionPerfilHabitos v) {
        clasificacion = v;
    }

    public void setLimiteInferior(BigDecimal v) {
        limiteInferior = v;
    }

    public void setLimiteSuperior(BigDecimal v) {
        limiteSuperior = v;
    }

    public void setIncluyeInferior(boolean v) {
        incluyeInferior = v;
    }

    public void setIncluyeSuperior(boolean v) {
        incluyeSuperior = v;
    }

    public void setOrden(Integer v) {
        orden = v;
    }
}
