package com.backend.nutri_predic.orientacion.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.plandia.entity.PlanDiario;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "adaptaciones_diarias")
public class AdaptacionDiaria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "cliente_id", nullable = false) private Cliente cliente;
    @OneToOne(optional = false) @JoinColumn(name = "prediccion_modelo_id", nullable = false, unique = true) private PrediccionModelo prediccionModelo;
    @ManyToOne @JoinColumn(name = "plan_diario_id") private PlanDiario planDiario;
    @Column(name = "fecha_evaluada", nullable = false) private LocalDate fechaEvaluada;
    @Column(name = "fecha_aplicacion", nullable = false) private LocalDate fechaAplicacion;
    @Column(name = "contenido_json", nullable = false, columnDefinition = "TEXT") private String contenidoJson;
    @Column(name = "creado_en", nullable = false, updatable = false) private Instant creadoEn = Instant.now();
    public String getContenidoJson() { return contenidoJson; }
    public void setCliente(Cliente v) { cliente = v; }
    public void setPrediccionModelo(PrediccionModelo v) { prediccionModelo = v; }
    public void setPlanDiario(PlanDiario v) { planDiario = v; }
    public void setFechaEvaluada(LocalDate v) { fechaEvaluada = v; }
    public void setFechaAplicacion(LocalDate v) { fechaAplicacion = v; }
    public void setContenidoJson(String v) { contenidoJson = v; }
}
