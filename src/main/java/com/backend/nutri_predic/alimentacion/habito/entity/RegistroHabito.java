package com.backend.nutri_predic.alimentacion.habito.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
        name = "registros_habitos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "fecha"}))
public class RegistroHabito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate fecha;

    private Integer cantidadComidas;
    private Double consumoAgua;
    /** @deprecated Fuente nutricional legacy; usar ComposicionNutricionalAlimento. Mantener columna por compatibilidad. */
    @Deprecated
    private Double proteinas;
    private String tipoAlimentacion;
    private String nivelOrganizacion;
    private Boolean desayuno;
    private Boolean snacks;

    /** @deprecated Texto libre legacy; usar RegistroAlimento estructurado. */
    @Deprecated
    @Column(length = 2000)
    private String alimentos;

    private Integer comidasCocinadas;

    @Column(length = 1000)
    private String restricciones;

    private Boolean consumeSuplementos;

    public RegistroHabito() {}

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Integer getCantidadComidas() {
        return cantidadComidas;
    }

    public Double getConsumoAgua() {
        return consumoAgua;
    }

    public Double getProteinas() {
        return proteinas;
    }

    public String getTipoAlimentacion() {
        return tipoAlimentacion;
    }

    public String getNivelOrganizacion() {
        return nivelOrganizacion;
    }

    public Boolean getDesayuno() {
        return desayuno;
    }

    public Boolean getSnacks() {
        return snacks;
    }

    public String getAlimentos() {
        return alimentos;
    }

    public Integer getComidasCocinadas() {
        return comidasCocinadas;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public Boolean getConsumeSuplementos() {
        return consumeSuplementos;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public void setFecha(LocalDate v) {
        fecha = v;
    }

    public void setCantidadComidas(Integer v) {
        cantidadComidas = v;
    }

    public void setConsumoAgua(Double v) {
        consumoAgua = v;
    }

    public void setProteinas(Double v) {
        proteinas = v;
    }

    public void setTipoAlimentacion(String v) {
        tipoAlimentacion = v;
    }

    public void setNivelOrganizacion(String v) {
        nivelOrganizacion = v;
    }

    public void setDesayuno(Boolean v) {
        desayuno = v;
    }

    public void setSnacks(Boolean v) {
        snacks = v;
    }

    public void setAlimentos(String v) {
        alimentos = v;
    }

    public void setComidasCocinadas(Integer v) {
        comidasCocinadas = v;
    }

    public void setRestricciones(String v) {
        restricciones = v;
    }

    public void setConsumeSuplementos(Boolean v) {
        consumeSuplementos = v;
    }
}
