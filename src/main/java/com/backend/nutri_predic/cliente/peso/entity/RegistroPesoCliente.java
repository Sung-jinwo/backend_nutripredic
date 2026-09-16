package com.backend.nutri_predic.cliente.peso.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "registros_peso_cliente", uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "fecha_medicion"}))
public class RegistroPesoCliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @Column(name = "fecha_medicion", nullable = false) private LocalDate fechaMedicion;
    @Column(name = "peso_kg", nullable = false, precision = 5, scale = 2) private BigDecimal pesoKg;
    @Column(name = "peso_anterior_kg", precision = 5, scale = 2) private BigDecimal pesoAnteriorKg;
    @Column(name = "variacion_kg", precision = 6, scale = 2) private BigDecimal variacionKg;
    @Column(name = "variacion_porcentual", precision = 8, scale = 2) private BigDecimal variacionPorcentual;
    @Column(name = "cambio_anomalo_confirmado", nullable = false) private boolean cambioAnomaloConfirmado;
    @Column(name = "creado_en", nullable = false, updatable = false) private Instant creadoEn = Instant.now();
    @Column(name = "actualizado_en") private Instant actualizadoEn;

    public Long getId() { return id; }
    public LocalDate getFechaMedicion() { return fechaMedicion; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public BigDecimal getPesoAnteriorKg() { return pesoAnteriorKg; }
    public BigDecimal getVariacionKg() { return variacionKg; }
    public BigDecimal getVariacionPorcentual() { return variacionPorcentual; }
    public boolean isCambioAnomaloConfirmado() { return cambioAnomaloConfirmado; }
    public void setCliente(Cliente v) { cliente = v; }
    public void setFechaMedicion(LocalDate v) { fechaMedicion = v; }
    public void setPesoKg(BigDecimal v) { pesoKg = v; }
    public void setPesoAnteriorKg(BigDecimal v) { pesoAnteriorKg = v; }
    public void setVariacionKg(BigDecimal v) { variacionKg = v; }
    public void setVariacionPorcentual(BigDecimal v) { variacionPorcentual = v; }
    public void setCambioAnomaloConfirmado(boolean v) { cambioAnomaloConfirmado = v; }
    public void setActualizadoEn(Instant v) { actualizadoEn = v; }
}
