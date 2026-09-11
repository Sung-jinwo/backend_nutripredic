package com.backend.nutri_predic.conocimiento.evaluacion.entity;

import com.backend.nutri_predic.conocimiento.entity.TemaConocimiento;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "resultados_tema_test",
        uniqueConstraints = @UniqueConstraint(columnNames = {"resultado_id", "tema"}))
public class ResultadoTemaTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "resultado_id")
    private ResultadoTest resultado;

    @Column(nullable = false)
    private String tema;

    @ManyToOne
    @JoinColumn(name = "tema_id")
    private TemaConocimiento temaEntidad;

    private int correctas;
    private int total;
    private double porcentaje;
    private BigDecimal puntajeObtenido;
    private BigDecimal puntajeMaximo;
    private Boolean requiereRefuerzo;
    private String criterioRefuerzoAplicado;

    public ResultadoTemaTest() {}

    public ResultadoTemaTest(ResultadoTest r, String t, int c, int total) {
        resultado = r;
        tema = t;
        correctas = c;
        this.total = total;
        porcentaje = total == 0 ? 0 : c * 100.0 / total;
    }

    public Long getId() {
        return id;
    }

    public String getTema() {
        return tema;
    }

    public int getCorrectas() {
        return correctas;
    }

    public int getTotal() {
        return total;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public TemaConocimiento getTemaEntidad() {
        return temaEntidad;
    }

    public BigDecimal getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public Boolean getRequiereRefuerzo() {
        return requiereRefuerzo;
    }

    public String getCriterioRefuerzoAplicado() {
        return criterioRefuerzoAplicado;
    }

    public void setTemaEntidad(TemaConocimiento v) {
        temaEntidad = v;
    }

    public void setPuntajeObtenido(BigDecimal v) {
        puntajeObtenido = v;
    }

    public void setPuntajeMaximo(BigDecimal v) {
        puntajeMaximo = v;
    }

    public void setRequiereRefuerzo(Boolean v) {
        requiereRefuerzo = v;
    }

    public void setCriterioRefuerzoAplicado(String v) {
        criterioRefuerzoAplicado = v;
    }
}
