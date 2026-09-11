package com.backend.nutri_predic.unidad.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "unidades_medida")
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false)
    private boolean activa = true;

    public UnidadMedida() {}

    public UnidadMedida(String c, String n) {
        codigo = c;
        nombre = n;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean v) {
        activa = v;
    }
}
