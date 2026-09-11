package com.backend.nutri_predic.requerimientonutricional.entity;

import com.backend.nutri_predic.common.enums.NivelActividadFisica;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "factores_actividad_regla", uniqueConstraints = @UniqueConstraint(columnNames = {"nivel_actividad", "version_referencia"}))
public class FactorActividadRegla {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(name = "nivel_actividad", nullable = false, length = 30) private NivelActividadFisica nivelActividad;
    @Column(nullable = false, precision = 6, scale = 4) private BigDecimal factor;
    @Column(nullable = false, length = 400) private String fuenteReferencia;
    @Column(nullable = false, length = 80) private String versionReferencia;
    private LocalDate vigenteDesde; private LocalDate vigenteHasta;
    @Enumerated(EnumType.STRING) private EstadoReglaRequerimientoNutricional estado = EstadoReglaRequerimientoNutricional.BORRADOR;
    private Boolean validada = false;
    public Long getId(){return id;} public NivelActividadFisica getNivelActividad(){return nivelActividad;} public BigDecimal getFactor(){return factor;}
    public String getFuenteReferencia(){return fuenteReferencia;} public String getVersionReferencia(){return versionReferencia;}
    public LocalDate getVigenteDesde(){return vigenteDesde;} public LocalDate getVigenteHasta(){return vigenteHasta;}
    public EstadoReglaRequerimientoNutricional getEstado(){return estado;} public Boolean getValidada(){return validada;}
    public void setNivelActividad(NivelActividadFisica v){nivelActividad=v;} public void setFactor(BigDecimal v){factor=v;}
    public void setFuenteReferencia(String v){fuenteReferencia=v;} public void setVersionReferencia(String v){versionReferencia=v;}
    public void setVigenteDesde(LocalDate v){vigenteDesde=v;} public void setVigenteHasta(LocalDate v){vigenteHasta=v;}
    public void setEstado(EstadoReglaRequerimientoNutricional v){estado=v;} public void setValidada(Boolean v){validada=v;}
}
