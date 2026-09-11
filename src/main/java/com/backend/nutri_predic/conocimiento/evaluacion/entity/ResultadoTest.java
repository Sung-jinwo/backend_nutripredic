package com.backend.nutri_predic.conocimiento.evaluacion.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.*;
import com.backend.nutri_predic.common.enums.NivelConocimiento;
import com.backend.nutri_predic.conocimiento.entity.InstrumentoConocimiento;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "resultados_tests")
public class ResultadoTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    private int totalPreguntas;
    private int correctas;
    private double porcentaje;

    @Enumerated(EnumType.STRING)
    private NivelConocimiento nivel;

    private Instant fecha = Instant.now();

    @ManyToOne
    @JoinColumn(name = "instrumento_id")
    private InstrumentoConocimiento instrumento;

    @ManyToOne
    @JoinColumn(name = "participacion_estudio_id")
    private ParticipacionEstudio participacionEstudio;

    @Enumerated(EnumType.STRING)
    private MomentoEvaluacion momento = MomentoEvaluacion.NO_DETERMINADO;

    @Enumerated(EnumType.STRING)
    private EstadoValidezMedicion estadoValidez = EstadoValidezMedicion.NO_DETERMINADA;

    private String motivoInvalidez;
    private BigDecimal puntajeObtenido;
    private BigDecimal puntajeMaximo;

    public ResultadoTest() {}

    public ResultadoTest(
            Cliente cliente, int total, int correctas, double porcentaje, NivelConocimiento nivel) {
        this.cliente = cliente;
        this.totalPreguntas = total;
        this.correctas = correctas;
        this.porcentaje = porcentaje;
        this.nivel = nivel;
    }

    public Long getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public int getTotalPreguntas() {
        return totalPreguntas;
    }

    public int getCorrectas() {
        return correctas;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public NivelConocimiento getNivel() {
        return nivel;
    }

    public Instant getFecha() {
        return fecha;
    }

    public InstrumentoConocimiento getInstrumento() {
        return instrumento;
    }

    public ParticipacionEstudio getParticipacionEstudio() {
        return participacionEstudio;
    }

    public MomentoEvaluacion getMomento() {
        return momento;
    }

    public EstadoValidezMedicion getEstadoValidez() {
        return estadoValidez;
    }

    public String getMotivoInvalidez() {
        return motivoInvalidez;
    }

    public BigDecimal getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public BigDecimal getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public void setInstrumento(InstrumentoConocimiento v) {
        instrumento = v;
    }

    public void setParticipacionEstudio(ParticipacionEstudio v) {
        participacionEstudio = v;
    }

    public void setMomento(MomentoEvaluacion v) {
        momento = v;
    }

    public void setEstadoValidez(EstadoValidezMedicion v) {
        estadoValidez = v;
    }

    public void setMotivoInvalidez(String v) {
        motivoInvalidez = v;
    }

    public void setPuntajeObtenido(BigDecimal v) {
        puntajeObtenido = v;
    }

    public void setPuntajeMaximo(BigDecimal v) {
        puntajeMaximo = v;
    }
}
