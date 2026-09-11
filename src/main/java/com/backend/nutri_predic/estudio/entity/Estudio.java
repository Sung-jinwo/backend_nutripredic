package com.backend.nutri_predic.estudio.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "estudios")
public class Estudio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEstudio estado = EstadoEstudio.BORRADOR;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Column(length = 2000)
    private String descripcion;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoEstudio getEstado() {
        return estado;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setCodigo(String v) {
        codigo = v;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setEstado(EstadoEstudio v) {
        estado = v;
    }

    public void setFechaInicio(LocalDate v) {
        fechaInicio = v;
    }

    public void setFechaFin(LocalDate v) {
        fechaFin = v;
    }

    public void setDescripcion(String v) {
        descripcion = v;
    }
}
