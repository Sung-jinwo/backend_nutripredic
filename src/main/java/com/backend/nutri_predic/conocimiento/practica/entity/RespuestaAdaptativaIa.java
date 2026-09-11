package com.backend.nutri_predic.conocimiento.practica.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "respuestas_adaptativas_ia",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"sesion_id", "pregunta_generada_ia_id"}))
public class RespuestaAdaptativaIa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionConocimientoIa sesion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pregunta_generada_ia_id", nullable = false)
    private PreguntaGeneradaIa preguntaGeneradaIa;

    @Column(nullable = false, length = 1)
    private String opcionSeleccionada;

    @Column(nullable = false)
    private boolean correcta;

    @Column(nullable = false, updatable = false)
    private Instant respondidaEn;

    protected RespuestaAdaptativaIa() {}

    public RespuestaAdaptativaIa(
            SesionConocimientoIa sesion,
            PreguntaGeneradaIa pregunta,
            String opcionSeleccionada,
            boolean correcta,
            Instant respondidaEn) {
        this.sesion = sesion;
        this.preguntaGeneradaIa = pregunta;
        this.opcionSeleccionada = opcionSeleccionada;
        this.correcta = correcta;
        this.respondidaEn = respondidaEn;
    }

    public Long getId() {
        return id;
    }

    public SesionConocimientoIa getSesion() {
        return sesion;
    }

    public PreguntaGeneradaIa getPreguntaGeneradaIa() {
        return preguntaGeneradaIa;
    }

    public String getOpcionSeleccionada() {
        return opcionSeleccionada;
    }

    public boolean isCorrecta() {
        return correcta;
    }

    public Instant getRespondidaEn() {
        return respondidaEn;
    }
}
