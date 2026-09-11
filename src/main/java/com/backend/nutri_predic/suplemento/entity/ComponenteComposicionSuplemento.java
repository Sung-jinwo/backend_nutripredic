package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.*;

@Entity
@Table(name = "componentes_composicion_suplementos")
public class ComponenteComposicionSuplemento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "composicion_suplemento_id")
    private ComposicionSuplemento composicion;

    @Enumerated(EnumType.STRING)
    private TipoComponenteSuplemento tipo;

    private String nombreOtro;
    private BigDecimal cantidad;

    @ManyToOne
    @JoinColumn(name = "unidad_id")
    private UnidadMedida unidad;

    public Long getId() {
        return id;
    }

    public ComposicionSuplemento getComposicion() {
        return composicion;
    }

    public TipoComponenteSuplemento getTipo() {
        return tipo;
    }

    public String getNombreOtro() {
        return nombreOtro;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public UnidadMedida getUnidad() {
        return unidad;
    }

    public void setComposicion(ComposicionSuplemento x) {
        composicion = x;
    }

    public void setTipo(TipoComponenteSuplemento x) {
        tipo = x;
    }

    public void setNombreOtro(String x) {
        nombreOtro = x;
    }

    public void setCantidad(BigDecimal x) {
        cantidad = x;
    }

    public void setUnidad(UnidadMedida x) {
        unidad = x;
    }
}
