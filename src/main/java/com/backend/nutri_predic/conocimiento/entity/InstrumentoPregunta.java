package com.backend.nutri_predic.conocimiento.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "instrumentos_preguntas",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"instrumento_id", "pregunta_id"}),
            @UniqueConstraint(columnNames = {"instrumento_id", "orden"})
        })
public class InstrumentoPregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instrumento_id")
    private InstrumentoConocimiento instrumento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pregunta_id")
    private PreguntaConocimiento pregunta;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal puntuacion;

    public Long getId() {
        return id;
    }

    public InstrumentoConocimiento getInstrumento() {
        return instrumento;
    }

    public PreguntaConocimiento getPregunta() {
        return pregunta;
    }

    public Integer getOrden() {
        return orden;
    }

    public BigDecimal getPuntuacion() {
        return puntuacion;
    }

    public void setInstrumento(InstrumentoConocimiento v) {
        instrumento = v;
    }

    public void setPregunta(PreguntaConocimiento v) {
        pregunta = v;
    }

    public void setOrden(Integer v) {
        orden = v;
    }

    public void setPuntuacion(BigDecimal v) {
        puntuacion = v;
    }
}
