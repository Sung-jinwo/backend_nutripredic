package com.backend.nutri_predic.actividadfisica.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity @Table(name="mapeos_categoria_actividad")
public class MapeoCategoriaActividad {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private String instrumentoCodigo,instrumentoVersion,resultadoOrigen,categoriaDestino,fuenteReferencia,versionReferencia;
 private Boolean validada=false; private LocalDate vigenciaDesde,vigenciaHasta;
 public String getInstrumentoCodigo(){return instrumentoCodigo;} public String getInstrumentoVersion(){return instrumentoVersion;} public String getResultadoOrigen(){return resultadoOrigen;} public String getCategoriaDestino(){return categoriaDestino;} public String getFuenteReferencia(){return fuenteReferencia;} public String getVersionReferencia(){return versionReferencia;} public Boolean getValidada(){return validada;} public LocalDate getVigenciaDesde(){return vigenciaDesde;} public LocalDate getVigenciaHasta(){return vigenciaHasta;}
}
