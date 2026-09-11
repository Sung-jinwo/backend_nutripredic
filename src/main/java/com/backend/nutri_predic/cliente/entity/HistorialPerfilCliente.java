package com.backend.nutri_predic.cliente.entity;

import com.backend.nutri_predic.common.enums.TipoObjetivoFisico;
import com.backend.nutri_predic.common.enums.SexoBiologico;
import com.backend.nutri_predic.common.enums.TipoEntrenamiento;
import com.backend.nutri_predic.common.enums.ObjetivoEnergetico;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "historial_perfiles_cliente")
public class HistorialPerfilCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    private Integer edad;
    @Enumerated(EnumType.STRING) private SexoBiologico sexo;

    @Column(precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(precision = 5, scale = 2)
    private BigDecimal alturaCm;

    private String objetivoFisico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_objetivo_fisico", length = 40)
    private TipoObjetivoFisico tipoObjetivoFisico;

    private Boolean realizaActividadFisica;

    private Integer diasEntrenamientoSemana;

    @Column(name = "tipo_actividad_fisica", length = 120)
    private String tipoActividadFisica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrenamiento", length = 30)
    private TipoEntrenamiento tipoEntrenamiento;

    private Integer duracionPromedioSesionMinutos;
    @Enumerated(EnumType.STRING) private ObjetivoEnergetico objetivoEnergetico;

    @Column(nullable = false)
    private LocalDate fechaDesde;

    /**
     * Fin de vigencia del snapshot. Null = versión vigente.
     * Se cierra al registrar una modificación posterior del perfil.
     */
    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public HistorialPerfilCliente() {}

    public HistorialPerfilCliente(Cliente c, LocalDate f) {
        cliente = c;
        edad = c.getEdad();
        sexo = c.getSexo();
        pesoKg = c.getPesoKg();
        alturaCm = c.getAlturaCm();
        objetivoFisico = c.getObjetivoFisico();
        tipoObjetivoFisico = c.getTipoObjetivoFisico();
        realizaActividadFisica = c.getRealizaActividadFisica();
        diasEntrenamientoSemana = c.getDiasEntrenamientoSemana();
        tipoActividadFisica = c.getTipoActividadFisica();
        tipoEntrenamiento = c.getTipoEntrenamiento();
        duracionPromedioSesionMinutos = c.getDuracionPromedioSesionMinutos();
        objetivoEnergetico = c.getObjetivoEnergetico();
        fechaDesde = f;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Integer getEdad() {
        return edad;
    }
    public SexoBiologico getSexo() { return sexo; }

    public BigDecimal getPesoKg() {
        return pesoKg;
    }

    public BigDecimal getAlturaCm() {
        return alturaCm;
    }

    public String getObjetivoFisico() {
        return objetivoFisico;
    }

    public TipoObjetivoFisico getTipoObjetivoFisico() {
        return tipoObjetivoFisico;
    }
    public Boolean getRealizaActividadFisica() { return realizaActividadFisica; }
    public Integer getDiasEntrenamientoSemana() { return diasEntrenamientoSemana; }
    public String getTipoActividadFisica() { return tipoActividadFisica; }
    public TipoEntrenamiento getTipoEntrenamiento() { return tipoEntrenamiento; }
    public Integer getDuracionPromedioSesionMinutos() { return duracionPromedioSesionMinutos; }
    public ObjetivoEnergetico getObjetivoEnergetico() { return objetivoEnergetico; }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public LocalDate getVigenteHasta() {
        return vigenteHasta;
    }

    public void setVigenteHasta(LocalDate v) {
        vigenteHasta = v;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public BigDecimal getImc() {
        if (pesoKg == null || alturaCm == null || alturaCm.signum() == 0) return null;
        var m = alturaCm.movePointLeft(2);
        return pesoKg.divide(m.multiply(m), 2, java.math.RoundingMode.HALF_UP);
    }
}
