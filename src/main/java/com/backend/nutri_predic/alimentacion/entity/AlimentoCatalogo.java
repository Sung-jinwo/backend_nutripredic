package com.backend.nutri_predic.alimentacion.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;

@Entity
@Table(name = "alimentos_catalogo")
public class AlimentoCatalogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    @ManyToOne
    @JoinColumn(name = "unidad_base_id")
    private UnidadMedida unidadBase;

    @Column(nullable = false)
    private boolean activo = true;

    public AlimentoCatalogo() {}

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public UnidadMedida getUnidadBase() {
        return unidadBase;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setCategoria(String v) {
        categoria = v;
    }

    public void setUnidadBase(UnidadMedida v) {
        unidadBase = v;
    }

    public void setActivo(boolean v) {
        activo = v;
    }
}
