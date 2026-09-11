package com.backend.nutri_predic.usuario.entity;

import com.backend.nutri_predic.common.enums.Rol;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    private Instant actualizadoEn = Instant.now();

    public Usuario() {}

    public Usuario(String email, String password, String nombre, Rol rol) {
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.rol = rol;
    }

    @PreUpdate
    void updated() {
        actualizadoEn = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    public String getNombre() {
        return nombre;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setPassword(String v) {
        password = v;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setActivo(boolean v) {
        activo = v;
    }
}
