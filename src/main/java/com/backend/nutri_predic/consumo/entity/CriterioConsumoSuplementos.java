package com.backend.nutri_predic.consumo.entity;

import com.backend.nutri_predic.consumo.classification.AmbitoAportePcs;
import com.backend.nutri_predic.consumo.classification.OperadorCriterioPcs;
import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "reglas_criterio_consumo")
public class CriterioConsumoSuplementos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_id", nullable = false)
    private RubricaConsumoSuplementos rubrica;

    @Column(nullable = false, length = 30)
    private String alcance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suplemento_catalogo_id")
    private SuplementoCatalogo suplemento;

    @Column(name = "componente_tipo", length = 50)
    private String componenteTipo;

    @Column(name = "elemento_aplicable")
    private String elementoAplicable;

    @Column(length = 100)
    private String metrica;

    @Column(name = "cantidad_referencia", precision = 18, scale = 6)
    private BigDecimal cantidadReferencia;

    @Column(name = "cantidad_referencia_hasta", precision = 18, scale = 6)
    private BigDecimal cantidadReferenciaHasta;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "ambito_aporte", length = 30)
    private AmbitoAportePcs ambitoAporte;

    @Column(name = "unidad_referencia", length = 30)
    private String unidadReferencia;

    @Column(name = "unidad_normalizada", length = 30)
    private String unidadNormalizada;

    @Column(length = 80)
    private String frecuencia;

    @Column(name = "numero_tomas")
    private Integer numeroTomas;

    @Column(name = "ventana_dias")
    private Integer ventanaDias;

    @Column(name = "requiere_composicion", nullable = false)
    private boolean requiereComposicion;

    @Column(name = "tipo_evaluador", length = 100)
    private String tipoEvaluador;

    @Column(length = 4000)
    private String parametros;

    @Column(name = "fuente_referencia_criterio", length = 1000)
    private String fuenteReferencia;

    @Column(name = "version_referencia", length = 100)
    private String versionReferencia;

    @Column(name = "observacion_metodologica", length = 2000)
    private String observacionMetodologica;

    public Long getId() {
        return id;
    }

    public RubricaConsumoSuplementos getRubrica() {
        return rubrica;
    }

    public String getAlcance() {
        return alcance;
    }

    public SuplementoCatalogo getSuplemento() {
        return suplemento;
    }

    public String getComponenteTipo() {
        return componenteTipo;
    }

    public String getElementoAplicable() {
        return elementoAplicable;
    }

    public String getMetrica() {
        return metrica;
    }

    public BigDecimal getCantidadReferencia() {
        return cantidadReferencia;
    }

    public BigDecimal getCantidadReferenciaHasta() {
        return cantidadReferenciaHasta;
    }

    public AmbitoAportePcs getAmbitoAporte() {
        return ambitoAporte;
    }

    public String getUnidadReferencia() {
        return unidadReferencia;
    }

    public String getUnidadNormalizada() {
        return unidadNormalizada;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public Integer getNumeroTomas() {
        return numeroTomas;
    }

    public Integer getVentanaDias() {
        return ventanaDias;
    }

    public boolean isRequiereComposicion() {
        return requiereComposicion;
    }

    public String getTipoEvaluador() {
        return tipoEvaluador;
    }

    public OperadorCriterioPcs getOperador() {
        if (tipoEvaluador == null) return null;
        try {
            return OperadorCriterioPcs.valueOf(tipoEvaluador);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String getParametros() {
        return parametros;
    }

    public String getFuenteReferencia() {
        return fuenteReferencia;
    }

    public String getVersionReferencia() {
        return versionReferencia;
    }

    public String getObservacionMetodologica() {
        return observacionMetodologica;
    }

    public void setRubrica(RubricaConsumoSuplementos valor) {
        rubrica = valor;
    }

    public void setAlcance(String valor) {
        alcance = valor;
    }

    public void setSuplemento(SuplementoCatalogo valor) {
        suplemento = valor;
    }

    public void setComponenteTipo(String valor) {
        componenteTipo = valor;
    }

    public void setElementoAplicable(String valor) {
        elementoAplicable = valor;
    }

    public void setMetrica(String valor) {
        metrica = valor;
    }

    public void setCantidadReferencia(BigDecimal valor) {
        cantidadReferencia = valor;
    }

    public void setCantidadReferenciaHasta(BigDecimal valor) {
        cantidadReferenciaHasta = valor;
    }

    public void setAmbitoAporte(AmbitoAportePcs valor) {
        ambitoAporte = valor;
    }

    public void setUnidadReferencia(String valor) {
        unidadReferencia = valor;
    }

    public void setUnidadNormalizada(String valor) {
        unidadNormalizada = valor;
    }

    public void setFrecuencia(String valor) {
        frecuencia = valor;
    }

    public void setNumeroTomas(Integer valor) {
        numeroTomas = valor;
    }

    public void setVentanaDias(Integer valor) {
        ventanaDias = valor;
    }

    public void setRequiereComposicion(boolean valor) {
        requiereComposicion = valor;
    }

    public void setTipoEvaluador(String valor) {
        tipoEvaluador = valor;
    }

    public void setOperador(OperadorCriterioPcs valor) {
        tipoEvaluador = valor == null ? null : valor.name();
    }

    public void setParametros(String valor) {
        parametros = valor;
    }

    public void setFuenteReferencia(String valor) {
        fuenteReferencia = valor;
    }

    public void setVersionReferencia(String valor) {
        versionReferencia = valor;
    }

    public void setObservacionMetodologica(String valor) {
        observacionMetodologica = valor;
    }
}
