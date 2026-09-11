package com.backend.nutri_predic.plandia.entity;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.LocalDate;
@Entity @Table(name="planes_diarios", uniqueConstraints=@UniqueConstraint(columnNames={"cliente_id","fecha_objetivo"}))
public class PlanDiario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="cliente_id") private Cliente cliente;
 @ManyToOne @JoinColumn(name="prediccion_modelo_origen_id") private PrediccionModelo prediccionModeloOrigen;
 @Column(nullable=false) private LocalDate fechaObjetivo;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private EstadoPlanDiario estado;
 @Column(name="agua_min_ml") private BigDecimal aguaMinMl;
 @Column(name="agua_max_ml") private BigDecimal aguaMaxMl;
  private BigDecimal energiaMinKcal,energiaMaxKcal; @Column(name="proteina_min_g") private BigDecimal proteinaMinG; @Column(name="proteina_max_g") private BigDecimal proteinaMaxG; @Column(name="carbohidratos_min_g") private BigDecimal carbohidratosMinG; @Column(name="carbohidratos_max_g") private BigDecimal carbohidratosMaxG; @Column(name="grasas_min_g") private BigDecimal grasasMinG; @Column(name="grasas_max_g") private BigDecimal grasasMaxG;
 @Column(length=1000) private String motivoNoDisponible; @Column(length=300) private String fuenteReferencia; @Column(length=100) private String versionReferencia;
 public Long getId(){return id;} public Cliente getCliente(){return cliente;} public PrediccionModelo getPrediccionModeloOrigen(){return prediccionModeloOrigen;} public LocalDate getFechaObjetivo(){return fechaObjetivo;} public EstadoPlanDiario getEstado(){return estado;} public BigDecimal getEnergiaMinKcal(){return energiaMinKcal;} public BigDecimal getEnergiaMaxKcal(){return energiaMaxKcal;} public BigDecimal getProteinaMinG(){return proteinaMinG;} public BigDecimal getProteinaMaxG(){return proteinaMaxG;} public BigDecimal getCarbohidratosMinG(){return carbohidratosMinG;} public BigDecimal getCarbohidratosMaxG(){return carbohidratosMaxG;} public BigDecimal getGrasasMinG(){return grasasMinG;} public BigDecimal getGrasasMaxG(){return grasasMaxG;} public String getMotivoNoDisponible(){return motivoNoDisponible;} public String getFuenteReferencia(){return fuenteReferencia;} public String getVersionReferencia(){return versionReferencia;}
 public void setCliente(Cliente v){cliente=v;} public void setPrediccionModeloOrigen(PrediccionModelo v){prediccionModeloOrigen=v;} public void setFechaObjetivo(LocalDate v){fechaObjetivo=v;} public void setEstado(EstadoPlanDiario v){estado=v;} public void setEnergiaMinKcal(BigDecimal v){energiaMinKcal=v;} public void setEnergiaMaxKcal(BigDecimal v){energiaMaxKcal=v;} public void setProteinaMinG(BigDecimal v){proteinaMinG=v;} public void setProteinaMaxG(BigDecimal v){proteinaMaxG=v;} public void setCarbohidratosMinG(BigDecimal v){carbohidratosMinG=v;} public void setCarbohidratosMaxG(BigDecimal v){carbohidratosMaxG=v;} public void setGrasasMinG(BigDecimal v){grasasMinG=v;} public void setGrasasMaxG(BigDecimal v){grasasMaxG=v;} public void setMotivoNoDisponible(String v){motivoNoDisponible=v;} public void setFuenteReferencia(String v){fuenteReferencia=v;} public void setVersionReferencia(String v){versionReferencia=v;}
 public BigDecimal getAguaMinMl(){return aguaMinMl;} public BigDecimal getAguaMaxMl(){return aguaMaxMl;}
 public void setAguaMinMl(BigDecimal v){aguaMinMl=v;} public void setAguaMaxMl(BigDecimal v){aguaMaxMl=v;}
}
