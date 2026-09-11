package com.backend.nutri_predic.prediccionmodelo.post;

import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fallos_ciclo_post_prediccion")
public class FalloCicloPostPrediccion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "prediccion_modelo_id") private PrediccionModelo prediccionModelo;
    @Column(nullable = false, length = 30) private String modulo;
    @Column(nullable = false, length = 160) private String tipoError;
    @Column(nullable = false) private Instant ocurridoEn = Instant.now();
    @Column(length = 1000) private String detalle;
    public void setPrediccionModelo(PrediccionModelo x) { prediccionModelo = x; }
    public void setModulo(String x) { modulo = x; }
    public void setTipoError(String x) { tipoError = x; }
    public void setDetalle(String x) { detalle = x; }
}
