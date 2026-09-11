package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "suplementos_cliente",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "suplemento_id"}))
public class SuplementoCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "suplemento_id")
    private SuplementoCatalogo suplemento;

    @Column(name = "nombre_declarado", nullable = false, length = 255)
    private String nombreDeclarado;

    private Double cantidad;
    private String unidad;
    private String frecuencia;
    @Column(name = "tiempo_uso") private String tiempoUso;

    @Column(name = "cantidad_por_toma", precision = 12, scale = 4)
    private BigDecimal cantidadPorToma;

    @ManyToOne
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidadMedida;

    @Column(name = "tomas_por_periodo") private Integer tomasPorPeriodo;

    @Column(name = "componentes_declarados", length = 1000)
    private String componentesDeclarados;
    @Column(name = "energia_kcal_por_toma", precision = 12, scale = 4) private BigDecimal energiaKcalPorToma;
    @Column(name = "proteina_g_por_toma", precision = 12, scale = 4) private BigDecimal proteinaGPorToma;
    @Column(name = "carbohidratos_g_por_toma", precision = 12, scale = 4) private BigDecimal carbohidratosGPorToma;
    @Column(name = "grasas_g_por_toma", precision = 12, scale = 4) private BigDecimal grasasGPorToma;
    @Column(name = "creatina_g_por_toma", precision = 12, scale = 4) private BigDecimal creatinaGPorToma;
    @Column(name = "cafeina_mg_por_toma", precision = 12, scale = 4) private BigDecimal cafeinaMgPorToma;
    @Column(name = "sodio_mg_por_toma", precision = 12, scale = 4) private BigDecimal sodioMgPorToma;

    @Column(name = "periodo_frecuencia")
    @Enumerated(EnumType.STRING)
    private PeriodoFrecuencia periodoFrecuencia;

    private Boolean activo = true;
    @Column(name = "fecha_inicio") private LocalDate fechaInicio;
    @Column(name = "fecha_fin") private LocalDate fechaFin;

    public SuplementoCliente() {}

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public SuplementoCatalogo getSuplemento() {
        return suplemento;
    }

    public String getNombreDeclarado() { return nombreDeclarado; }

    public Double getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public String getTiempoUso() {
        return tiempoUso;
    }

    public BigDecimal getCantidadPorToma() {
        return cantidadPorToma;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public Integer getTomasPorPeriodo() {
        return tomasPorPeriodo;
    }
    public String getComponentesDeclarados() { return componentesDeclarados; }
    public BigDecimal getEnergiaKcalPorToma() { return energiaKcalPorToma; }
    public BigDecimal getProteinaGPorToma() { return proteinaGPorToma; }
    public BigDecimal getCarbohidratosGPorToma() { return carbohidratosGPorToma; }
    public BigDecimal getGrasasGPorToma() { return grasasGPorToma; }
    public BigDecimal getCreatinaGPorToma() { return creatinaGPorToma; }
    public BigDecimal getCafeinaMgPorToma() { return cafeinaMgPorToma; }
    public BigDecimal getSodioMgPorToma() { return sodioMgPorToma; }

    public PeriodoFrecuencia getPeriodoFrecuencia() {
        return periodoFrecuencia;
    }

    public Boolean getActivo() {
        return activo;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public void setSuplemento(SuplementoCatalogo v) {
        suplemento = v;
    }

    public void setNombreDeclarado(String v) { nombreDeclarado = v; }

    public void setCantidad(Double v) {
        cantidad = v;
    }

    public void setUnidad(String v) {
        unidad = v;
    }

    public void setFrecuencia(String v) {
        frecuencia = v;
    }

    public void setTiempoUso(String v) {
        tiempoUso = v;
    }

    public void setCantidadPorToma(BigDecimal v) {
        cantidadPorToma = v;
    }

    public void setUnidadMedida(UnidadMedida v) {
        unidadMedida = v;
    }

    public void setTomasPorPeriodo(Integer v) {
        tomasPorPeriodo = v;
    }
    public void setComponentesDeclarados(String v) { componentesDeclarados = v; }
    public void setEnergiaKcalPorToma(BigDecimal v) { energiaKcalPorToma = v; }
    public void setProteinaGPorToma(BigDecimal v) { proteinaGPorToma = v; }
    public void setCarbohidratosGPorToma(BigDecimal v) { carbohidratosGPorToma = v; }
    public void setGrasasGPorToma(BigDecimal v) { grasasGPorToma = v; }
    public void setCreatinaGPorToma(BigDecimal v) { creatinaGPorToma = v; }
    public void setCafeinaMgPorToma(BigDecimal v) { cafeinaMgPorToma = v; }
    public void setSodioMgPorToma(BigDecimal v) { sodioMgPorToma = v; }

    public void setPeriodoFrecuencia(PeriodoFrecuencia v) {
        periodoFrecuencia = v;
    }

    public void setActivo(Boolean v) {
        activo = v;
    }

    public void setFechaInicio(LocalDate v) {
        fechaInicio = v;
    }

    public void setFechaFin(LocalDate v) {
        fechaFin = v;
    }
}
