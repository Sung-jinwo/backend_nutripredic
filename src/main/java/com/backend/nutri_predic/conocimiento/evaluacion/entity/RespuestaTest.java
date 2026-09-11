package com.backend.nutri_predic.conocimiento.evaluacion.entity;

import com.backend.nutri_predic.conocimiento.entity.PreguntaConocimiento;
import jakarta.persistence.*;

@Entity
@Table(name = "respuestas_test")
public class RespuestaTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ResultadoTest resultado;

    @ManyToOne(optional = false)
    private PreguntaConocimiento pregunta;

    @Column(nullable = false)
    private String respuesta;

    private boolean correcta;

    public RespuestaTest() {}

    public RespuestaTest(
            ResultadoTest resultado,
            PreguntaConocimiento pregunta,
            String respuesta,
            boolean correcta) {
        this.resultado = resultado;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
        this.correcta = correcta;
    }

    public Long getId() {
        return id;
    }

    public PreguntaConocimiento getPregunta() {
        return pregunta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public boolean isCorrecta() {
        return correcta;
    }
}
