package com.backend.nutri_predic.conocimiento.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "preguntas_conocimiento",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_version", "version"}))
public class PreguntaConocimiento {
    public enum EstadoPregunta {
        ACTIVA,
        INACTIVA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String enunciado;

    @Column(name = "opcion_a", nullable = false)
    private String opcionA;

    @Column(name = "opcion_b", nullable = false)
    private String opcionB;

    @Column(name = "opcion_c", nullable = false)
    private String opcionC;

    @Column(name = "opcion_d", nullable = false)
    private String opcionD;

    @Column(nullable = false)
    private String respuestaCorrecta;

    private String categoria;
    private String dificultad;

    @Column(name = "grupo_version", nullable = false, length = 36)
    private String grupoVersion;

    @Column(nullable = false)
    private Integer version = 1;

    private String tema;

    @ManyToOne
    @JoinColumn(name = "tema_id")
    private TemaConocimiento temaEntidad;

    private String subtema;

    @Column(length = 2000)
    private String explicacion;

    @Column(name = "fuente_referencia", length = 1000)
    private String fuenteReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPregunta estadoPregunta = EstadoPregunta.ACTIVA;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(nullable = false)
    private Instant vigenteDesde;

    public PreguntaConocimiento() {}

    @PrePersist
    void pre() {
        if (grupoVersion == null) grupoVersion = UUID.randomUUID().toString();
        if (version == null) version = 1;
        if (estadoPregunta == null) estadoPregunta = EstadoPregunta.ACTIVA;
        if (creadoEn == null) creadoEn = Instant.now();
        if (vigenteDesde == null) vigenteDesde = creadoEn;
    }

    public Long getId() {
        return id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getOpcionA() {
        return opcionA;
    }

    public String getOpcionB() {
        return opcionB;
    }

    public String getOpcionC() {
        return opcionC;
    }

    public String getOpcionD() {
        return opcionD;
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDificultad() {
        return dificultad;
    }

    public String getGrupoVersion() {
        return grupoVersion;
    }

    public Integer getVersion() {
        return version;
    }

    public String getTema() {
        return temaEntidad != null ? temaEntidad.getNombre() : (tema == null ? categoria : tema);
    }

    public TemaConocimiento getTemaEntidad() {
        return temaEntidad;
    }

    public String getSubtema() {
        return subtema;
    }

    public String getExplicacion() {
        return explicacion;
    }

    public String getFuenteReferencia() {
        return fuenteReferencia;
    }

    public EstadoPregunta getEstadoPregunta() {
        return estadoPregunta;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getVigenteDesde() {
        return vigenteDesde;
    }

    public void setEnunciado(String v) {
        enunciado = v;
    }

    public void setOpcionA(String v) {
        opcionA = v;
    }

    public void setOpcionB(String v) {
        opcionB = v;
    }

    public void setOpcionC(String v) {
        opcionC = v;
    }

    public void setOpcionD(String v) {
        opcionD = v;
    }

    public void setRespuestaCorrecta(String v) {
        respuestaCorrecta = v;
    }

    public void setCategoria(String v) {
        categoria = v;
    }

    public void setDificultad(String v) {
        dificultad = v;
    }

    public void setGrupoVersion(String v) {
        grupoVersion = v;
    }

    public void setVersion(Integer v) {
        version = v;
    }

    public void setTema(String v) {
        tema = v;
    }

    public void setTemaEntidad(TemaConocimiento v) {
        temaEntidad = v;
    }

    public void setSubtema(String v) {
        subtema = v;
    }

    public void setExplicacion(String v) {
        explicacion = v;
    }

    public void setFuenteReferencia(String v) {
        fuenteReferencia = v;
    }

    public void setEstadoPregunta(EstadoPregunta v) {
        estadoPregunta = v;
    }

    public void setVigenteDesde(Instant v) {
        vigenteDesde = v;
    }
}
