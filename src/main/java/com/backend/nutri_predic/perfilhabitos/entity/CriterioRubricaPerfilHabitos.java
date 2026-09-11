package com.backend.nutri_predic.perfilhabitos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "criterios_rubrica_perfil_habitos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"rubrica_id", "codigo"}))
public class CriterioRubricaPerfilHabitos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rubrica_id")
    private RubricaPerfilHabitos rubrica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dimension_id")
    private DimensionRubricaPerfilHabitos dimension;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoEvaluacionCriterioPerfil tipoEvaluacion;

    @Column(length = 80)
    private String componente;

    @Column(length = 80)
    private String fuenteDatos;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal puntosMaximos;

    @Column(length = 4000)
    private String parametrosJson;

    @Column(length = 2000)
    private String adherencia7dJson;

    @Column(length = 1000)
    private String fuente;

    @Column(length = 255)
    private String organismoAutor;

    @Column(length = 80)
    private String versionAnio;

    @Column(length = 1000)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoFuenteCriterioPerfil tipoFuente;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public RubricaPerfilHabitos getRubrica() { return rubrica; }
    public DimensionRubricaPerfilHabitos getDimension() { return dimension; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public TipoEvaluacionCriterioPerfil getTipoEvaluacion() { return tipoEvaluacion; }
    public String getComponente() { return componente; }
    public String getFuenteDatos() { return fuenteDatos; }
    public BigDecimal getPuntosMaximos() { return puntosMaximos; }
    public String getParametrosJson() { return parametrosJson; }
    public String getAdherencia7dJson() { return adherencia7dJson; }
    public String getFuente() { return fuente; }
    public String getOrganismoAutor() { return organismoAutor; }
    public String getVersionAnio() { return versionAnio; }
    public String getReferencia() { return referencia; }
    public TipoFuenteCriterioPerfil getTipoFuente() { return tipoFuente; }
    public Integer getOrden() { return orden; }
    public boolean isActivo() { return activo; }

    public void setRubrica(RubricaPerfilHabitos v) { rubrica = v; }
    public void setDimension(DimensionRubricaPerfilHabitos v) { dimension = v; }
    public void setCodigo(String v) { codigo = v; }
    public void setNombre(String v) { nombre = v; }
    public void setTipoEvaluacion(TipoEvaluacionCriterioPerfil v) { tipoEvaluacion = v; }
    public void setComponente(String v) { componente = v; }
    public void setFuenteDatos(String v) { fuenteDatos = v; }
    public void setPuntosMaximos(BigDecimal v) { puntosMaximos = v; }
    public void setParametrosJson(String v) { parametrosJson = v; }
    public void setAdherencia7dJson(String v) { adherencia7dJson = v; }
    public void setFuente(String v) { fuente = v; }
    public void setOrganismoAutor(String v) { organismoAutor = v; }
    public void setVersionAnio(String v) { versionAnio = v; }
    public void setReferencia(String v) { referencia = v; }
    public void setTipoFuente(TipoFuenteCriterioPerfil v) { tipoFuente = v; }
    public void setOrden(Integer v) { orden = v; }
    public void setActivo(boolean v) { activo = v; }
}
