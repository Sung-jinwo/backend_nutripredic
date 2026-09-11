package com.backend.nutri_predic.estudio.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
        name = "participaciones_estudio",
        uniqueConstraints = @UniqueConstraint(columnNames = {"estudio_id", "cliente_id"}))
public class ParticipacionEstudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "estudio_id")
    private Estudio estudio;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "codigo_participante", nullable = false)
    private String codigoParticipante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrupoEstudio grupo;

    private LocalDate fechaAsignacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEstudio estado = EstadoEstudio.BORRADOR;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public Estudio getEstudio() {
        return estudio;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getCodigoParticipante() {
        return codigoParticipante;
    }

    public GrupoEstudio getGrupo() {
        return grupo;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public EstadoEstudio getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setEstudio(Estudio v) {
        estudio = v;
    }

    public void setCliente(Cliente v) {
        cliente = v;
    }

    public void setCodigoParticipante(String v) {
        codigoParticipante = v;
    }

    public void setGrupo(GrupoEstudio v) {
        grupo = v;
    }

    public void setFechaAsignacion(LocalDate v) {
        fechaAsignacion = v;
    }

    public void setEstado(EstadoEstudio v) {
        estado = v;
    }
}
