package com.backend.nutri_predic.alimentacion.entity;

import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "registros_alimentos")
public class RegistroAlimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "registro_habito_id")
    private RegistroHabito registroHabito;

    @ManyToOne
    @JoinColumn(name = "alimento_id")
    private AlimentoCatalogo alimento;

    @Column(name = "nombre_registrado", nullable = false, length = 200)
    private String nombreRegistrado;

    @ManyToOne
    @JoinColumn(name = "composicion_nutricional_id")
    private ComposicionNutricionalAlimento composicionNutricional;

    @ManyToOne
    @JoinColumn(name = "equivalencia_unidad_id")
    private EquivalenciaUnidadAlimento equivalenciaUnidad;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal cantidad;
    @Column(name = "kcal_registrada", precision = 12, scale = 4) private BigDecimal kcalRegistrada;
    @Column(name = "proteina_g_registrada", precision = 12, scale = 4) private BigDecimal proteinaGRegistrada;
    @Column(name = "carbohidratos_g_registrados", precision = 12, scale = 4) private BigDecimal carbohidratosGRegistrados;
    @Column(name = "grasas_g_registradas", precision = 12, scale = 4) private BigDecimal grasasGRegistradas;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedida unidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentoComida momentoComida;

    public RegistroAlimento() {}

    public Long getId() {
        return id;
    }

    public RegistroHabito getRegistroHabito() {
        return registroHabito;
    }

    public AlimentoCatalogo getAlimento() {
        return alimento;
    }

    public String getNombreRegistrado() { return nombreRegistrado; }

    public ComposicionNutricionalAlimento getComposicionNutricional() {
        return composicionNutricional;
    }

    public EquivalenciaUnidadAlimento getEquivalenciaUnidad() {
        return equivalenciaUnidad;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }
    public BigDecimal getKcalRegistrada() { return kcalRegistrada; }
    public BigDecimal getProteinaGRegistrada() { return proteinaGRegistrada; }
    public BigDecimal getCarbohidratosGRegistrados() { return carbohidratosGRegistrados; }
    public BigDecimal getGrasasGRegistradas() { return grasasGRegistradas; }

    public UnidadMedida getUnidad() {
        return unidad;
    }

    public MomentoComida getMomentoComida() {
        return momentoComida;
    }

    public void setRegistroHabito(RegistroHabito v) {
        registroHabito = v;
    }

    public void setAlimento(AlimentoCatalogo v) {
        alimento = v;
    }

    public void setNombreRegistrado(String v) { nombreRegistrado = v; }

    public void setComposicionNutricional(ComposicionNutricionalAlimento v) {
        composicionNutricional = v;
    }

    public void setEquivalenciaUnidad(EquivalenciaUnidadAlimento v) {
        equivalenciaUnidad = v;
    }

    public void setCantidad(BigDecimal v) {
        cantidad = v;
    }
    public void setKcalRegistrada(BigDecimal v) { kcalRegistrada = v; }
    public void setProteinaGRegistrada(BigDecimal v) { proteinaGRegistrada = v; }
    public void setCarbohidratosGRegistrados(BigDecimal v) { carbohidratosGRegistrados = v; }
    public void setGrasasGRegistradas(BigDecimal v) { grasasGRegistradas = v; }

    public void setUnidad(UnidadMedida v) {
        unidad = v;
    }

    public void setMomentoComida(MomentoComida v) {
        momentoComida = v;
    }
}
