package com.backend.nutri_predic.cliente.entity;

import com.backend.nutri_predic.common.enums.*;
import com.backend.nutri_predic.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    private Integer edad;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private SexoBiologico sexo;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    @Column(name = "altura_cm", precision = 5, scale = 2)
    private BigDecimal alturaCm;

    @Column(name = "objetivo_fisico", length = 120)
    private String objetivoFisico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_objetivo_fisico", length = 40)
    private TipoObjetivoFisico tipoObjetivoFisico;

    @Column(name = "realiza_actividad_fisica")
    private Boolean realizaActividadFisica;

    @Column(name = "dias_entrenamiento_semana")
    private Integer diasEntrenamientoSemana;

    @Column(name = "tipo_actividad_fisica", length = 120)
    private String tipoActividadFisica;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_entrenamiento", length = 30)
    private TipoEntrenamiento tipoEntrenamiento;

    @Column(name = "duracion_promedio_sesion_minutos")
    private Integer duracionPromedioSesionMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "objetivo_energetico", length = 20)
    private ObjetivoEnergetico objetivoEnergetico;

    @Enumerated(EnumType.STRING)
    private EstadoCliente estado = EstadoCliente.ACTIVO;

    @Column(name = "dataset_group_id", nullable = false, unique = true)
    private java.util.UUID datasetGroupId = java.util.UUID.randomUUID();

    private Instant creadoEn = Instant.now();

    private Instant actualizadoEn = Instant.now();

    public Cliente() {}

    public Cliente(Usuario u) {
        usuario = u;
    }

    @PreUpdate
    private void alActualizar() {
        actualizadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Integer getEdad() {
        return edad;
    }

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
    public SexoBiologico getSexo() { return sexo; }

    public Boolean getRealizaActividadFisica() { return realizaActividadFisica; }
    public Integer getDiasEntrenamientoSemana() { return diasEntrenamientoSemana; }
    public String getTipoActividadFisica() { return tipoActividadFisica; }
    public TipoEntrenamiento getTipoEntrenamiento() { return tipoEntrenamiento; }
    public Integer getDuracionPromedioSesionMinutos() { return duracionPromedioSesionMinutos; }
    public ObjetivoEnergetico getObjetivoEnergetico() { return objetivoEnergetico; }

    public EstadoCliente getEstado() {
        return estado;
    }

    public BigDecimal getImc() {
        if (pesoKg == null || alturaCm == null || alturaCm.signum() == 0) return null;
        BigDecimal metros = alturaCm.movePointLeft(2);
        return pesoKg.divide(metros.multiply(metros), 2, RoundingMode.HALF_UP);
    }

    public java.util.UUID getDatasetGroupId() {
        return datasetGroupId;
    }

    public void setEdad(Integer v) {
        edad = v;
    }

    public void setPesoKg(BigDecimal v) {
        pesoKg = v;
    }

    public void setAlturaCm(BigDecimal v) {
        alturaCm = v;
    }

    public void setObjetivoFisico(String v) {
        objetivoFisico = v;
    }

    public void setTipoObjetivoFisico(TipoObjetivoFisico v) {
        tipoObjetivoFisico = v;
    }
    public void setSexo(SexoBiologico v) { sexo = v; }
    public void setRealizaActividadFisica(Boolean v) { realizaActividadFisica = v; }
    public void setDiasEntrenamientoSemana(Integer v) { diasEntrenamientoSemana = v; }
    public void setTipoActividadFisica(String v) { tipoActividadFisica = v; }
    public void setTipoEntrenamiento(TipoEntrenamiento v) { tipoEntrenamiento = v; }
    public void setDuracionPromedioSesionMinutos(Integer v) { duracionPromedioSesionMinutos = v; }
    public void setObjetivoEnergetico(ObjetivoEnergetico v) { objetivoEnergetico = v; }

    public void setEstado(EstadoCliente v) {
        estado = v;
    }
}
