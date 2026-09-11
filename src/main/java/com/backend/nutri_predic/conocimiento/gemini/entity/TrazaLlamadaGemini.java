package com.backend.nutri_predic.conocimiento.gemini.entity;

import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "trazas_llamadas_gemini")
public class TrazaLlamadaGemini {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prediccion_modelo_id")
    private PrediccionModelo prediccionModelo;

    @Column(nullable = false)
    private Instant fecha = Instant.now();

    @Column(nullable = false)
    private boolean exito;

    @Column(nullable = false)
    private int cantidadPreguntas;

    @Column(nullable = false)
    private boolean reutilizada;

    public TrazaLlamadaGemini() {}

    public TrazaLlamadaGemini(PrediccionModelo p, boolean e, int c, boolean r) {
        prediccionModelo = p;
        exito = e;
        cantidadPreguntas = c;
        reutilizada = r;
    }

    public boolean isReutilizada() {
        return reutilizada;
    }
}
