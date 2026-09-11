package com.backend.nutri_predic.conocimiento.practica.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "preguntas_generadas_ia",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sesion_id", "orden"}))
public class PreguntaGeneradaIa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sesion_id")
    private SesionConocimientoIa sesion;

    @Column(nullable = false)
    private String tema;

    private String subtema;

    @Column(nullable = false)
    private String dificultad;

    @Column(nullable = false, length = 2000)
    private String enunciado;

    @Column(name = "opcion_a", nullable = false, length = 1000)
    private String opcionA;

    @Column(name = "opcion_b", nullable = false, length = 1000)
    private String opcionB;

    @Column(name = "opcion_c", nullable = false, length = 1000)
    private String opcionC;

    @Column(name = "opcion_d", nullable = false, length = 1000)
    private String opcionD;

    @Column(nullable = false)
    private String respuestaCorrecta;

    @Column(nullable = false, length = 2000)
    private String explicacion;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false, updatable = false)
    private Instant creadaEn = Instant.now();

    public Long getId() {
        return id;
    }

    public SesionConocimientoIa getSesion() {
        return sesion;
    }

    public String getTema() {
        return tema;
    }

    public String getSubtema() {
        return subtema;
    }

    public String getDificultad() {
        return dificultad;
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

    public String getExplicacion() {
        return explicacion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setSesion(SesionConocimientoIa v) {
        sesion = v;
    }

    public void setTema(String v) {
        tema = v;
    }

    public void setSubtema(String v) {
        subtema = v;
    }

    public void setDificultad(String v) {
        dificultad = v;
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

    public void setExplicacion(String v) {
        explicacion = v;
    }

    public void setOrden(Integer v) {
        orden = v;
    }
}
