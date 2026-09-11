package com.backend.nutri_predic.conocimiento.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "temas_conocimiento")
public class TemaConocimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private boolean activo = true;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setCodigo(String v) {
        codigo = v;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setActivo(boolean v) {
        activo = v;
    }
}
