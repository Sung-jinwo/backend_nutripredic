package com.backend.nutri_predic.actividadfisica.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name = "evaluaciones_actividad_fisica")
public class EvaluacionActividadFisica {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="cliente_id") private Cliente cliente;
 @Column(nullable=false) private LocalDate fechaEvaluacion;
 @Column(nullable=false,length=100) private String instrumentoCodigo;
 @Column(nullable=false,length=60) private String instrumentoVersion;
 @Column(nullable=false,length=100) private String nivelResultado;
 private BigDecimal valorResultado;
 @Column(nullable=false,length=300) private String fuenteReferencia;
 private Boolean validada=false; private LocalDate vigenciaDesde,vigenciaHasta;
 public Long getId(){return id;} public Cliente getCliente(){return cliente;} public LocalDate getFechaEvaluacion(){return fechaEvaluacion;} public String getInstrumentoCodigo(){return instrumentoCodigo;} public String getInstrumentoVersion(){return instrumentoVersion;} public String getNivelResultado(){return nivelResultado;} public BigDecimal getValorResultado(){return valorResultado;} public String getFuenteReferencia(){return fuenteReferencia;} public Boolean getValidada(){return validada;} public LocalDate getVigenciaDesde(){return vigenciaDesde;} public LocalDate getVigenciaHasta(){return vigenciaHasta;}
 public void setCliente(Cliente x){cliente=x;} public void setFechaEvaluacion(LocalDate x){fechaEvaluacion=x;} public void setInstrumentoCodigo(String x){instrumentoCodigo=x;} public void setInstrumentoVersion(String x){instrumentoVersion=x;} public void setNivelResultado(String x){nivelResultado=x;} public void setValorResultado(BigDecimal x){valorResultado=x;} public void setFuenteReferencia(String x){fuenteReferencia=x;} public void setValidada(Boolean x){validada=x;} public void setVigenciaDesde(LocalDate x){vigenciaDesde=x;} public void setVigenciaHasta(LocalDate x){vigenciaHasta=x;}
}
