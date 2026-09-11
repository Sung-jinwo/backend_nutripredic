package com.backend.nutri_predic.perfilhabitos.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.usuario.entity.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "evaluaciones_perfil_habitos")
public class EvaluacionPerfilHabitos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rubrica_id")
    private RubricaPerfilHabitos rubrica;

    @ManyToOne(optional = false)
    @JoinColumn(name = "evaluador_usuario_id")
    private Usuario evaluador;

    @Column(nullable = false, updatable = false)
    private Instant fechaEvaluacion = Instant.now();

    @Column(nullable = false)
    private LocalDate fechaCorte;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal puntajeTotal;

    @Column(precision = 6, scale = 2)
    private BigDecimal puntajeMaximoCalculable;

    @Column(precision = 6, scale = 2)
    private BigDecimal coberturaCalculable;

    @Column(length = 1000)
    private String motivoNoValida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClasificacionPerfilHabitos clasificacionReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoValidezMedicion estadoValidez;

    @Column(length = 2000)
    private String observacion;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public RubricaPerfilHabitos getRubrica() {
        return rubrica;
    }

    public Usuario getEvaluador() {
        return evaluador;
    }

    public Instant getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public BigDecimal getPuntajeTotal() {
        return puntajeTotal;
    }

    public ClasificacionPerfilHabitos getClasificacionReal() {
        return clasificacionReal;
    }

    public BigDecimal getPuntajeMaximoCalculable() {
        return puntajeMaximoCalculable;
    }

    public BigDecimal getCoberturaCalculable() {
        return coberturaCalculable;
    }

    public String getMotivoNoValida() {
        return motivoNoValida;
    }

    public EstadoValidezMedicion getEstadoValidez() {
        return estadoValidez;
    }

    public String getObservacion() {
        return observacion;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public void setRubrica(RubricaPerfilHabitos v) {
        rubrica = v;
    }

    public void setEvaluador(Usuario v) {
        evaluador = v;
    }

    public void setFechaCorte(LocalDate v) {
        fechaCorte = v;
    }

    public void setPuntajeTotal(BigDecimal v) {
        puntajeTotal = v;
    }

    public void setClasificacionReal(ClasificacionPerfilHabitos v) {
        clasificacionReal = v;
    }

    public void setPuntajeMaximoCalculable(BigDecimal v) {
        puntajeMaximoCalculable = v;
    }

    public void setCoberturaCalculable(BigDecimal v) {
        coberturaCalculable = v;
    }

    public void setMotivoNoValida(String v) {
        motivoNoValida = v;
    }

    public void setEstadoValidez(EstadoValidezMedicion v) {
        estadoValidez = v;
    }

    public void setObservacion(String v) {
        observacion = v;
    }
}
