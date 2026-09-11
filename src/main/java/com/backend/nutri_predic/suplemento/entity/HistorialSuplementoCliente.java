package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.*;
import java.time.*;

@Entity
@Table(name = "historial_suplementos_cliente")
public class HistorialSuplementoCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asignacion_id")
    private SuplementoCliente asignacion;

    private Double cantidad;
    private String unidad;
    private String frecuencia;
    private String tiempoUso;

    @Column(precision = 12, scale = 4)
    private BigDecimal cantidadPorToma;

    @ManyToOne private UnidadMedida unidadMedida;
    private Integer tomasPorPeriodo;

    @Enumerated(EnumType.STRING)
    private PeriodoFrecuencia periodoFrecuencia;

    private Boolean activo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Column(nullable = false, updatable = false)
    private Instant registradoEn = Instant.now();

    public HistorialSuplementoCliente() {}

    public HistorialSuplementoCliente(SuplementoCliente s) {
        asignacion = s;
        cantidad = s.getCantidad();
        unidad = s.getUnidad();
        frecuencia = s.getFrecuencia();
        tiempoUso = s.getTiempoUso();
        cantidadPorToma = s.getCantidadPorToma();
        unidadMedida = s.getUnidadMedida();
        tomasPorPeriodo = s.getTomasPorPeriodo();
        periodoFrecuencia = s.getPeriodoFrecuencia();
        activo = s.getActivo();
        fechaInicio = s.getFechaInicio();
        fechaFin = s.getFechaFin();
    }

    public Long getId() {
        return id;
    }

    public SuplementoCliente getAsignacion() {
        return asignacion;
    }

    public Boolean getActivo() {
        return activo;
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

    public PeriodoFrecuencia getPeriodoFrecuencia() {
        return periodoFrecuencia;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Instant getRegistradoEn() {
        return registradoEn;
    }
}
