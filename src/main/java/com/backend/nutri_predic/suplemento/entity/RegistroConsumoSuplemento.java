package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "registros_consumo_suplemento",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"registro_habito_id", "suplemento_cliente_id"}))
public class RegistroConsumoSuplemento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "registro_habito_id")
    private RegistroHabito registroHabito;

    @ManyToOne(optional = false)
    @JoinColumn(name = "suplemento_cliente_id")
    private SuplementoCliente suplementoCliente;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidadConsumida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidad;

    private Integer numeroTomas;

    @ManyToOne
    @JoinColumn(name = "composicion_suplemento_id")
    private ComposicionSuplemento composicionSuplemento;

    @ManyToOne
    @JoinColumn(name = "equivalencia_unidad_suplemento_id")
    private EquivalenciaUnidadSuplemento equivalenciaUnidad;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public RegistroConsumoSuplemento() {}

    public Long getId() {
        return id;
    }

    public RegistroHabito getRegistroHabito() {
        return registroHabito;
    }

    public SuplementoCliente getSuplementoCliente() {
        return suplementoCliente;
    }

    public BigDecimal getCantidadConsumida() {
        return cantidadConsumida;
    }

    public UnidadMedida getUnidad() {
        return unidad;
    }

    public Integer getNumeroTomas() {
        return numeroTomas;
    }

    public ComposicionSuplemento getComposicionSuplemento() {
        return composicionSuplemento;
    }

    public EquivalenciaUnidadSuplemento getEquivalenciaUnidad() {
        return equivalenciaUnidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setRegistroHabito(RegistroHabito v) {
        registroHabito = v;
    }

    public void setSuplementoCliente(SuplementoCliente v) {
        suplementoCliente = v;
    }

    public void setCantidadConsumida(BigDecimal v) {
        cantidadConsumida = v;
    }

    public void setUnidad(UnidadMedida v) {
        unidad = v;
    }

    public void setNumeroTomas(Integer v) {
        numeroTomas = v;
    }

    public void setComposicionSuplemento(ComposicionSuplemento v) {
        composicionSuplemento = v;
    }

    public void setEquivalenciaUnidad(EquivalenciaUnidadSuplemento v) {
        equivalenciaUnidad = v;
    }

    public void setObservacion(String v) {
        observacion = v;
    }
}
