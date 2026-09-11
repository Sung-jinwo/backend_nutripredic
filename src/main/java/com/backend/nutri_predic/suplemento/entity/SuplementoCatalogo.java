package com.backend.nutri_predic.suplemento.entity;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import jakarta.persistence.*;

@Entity
@Table(name = "suplementos_catalogo")
public class SuplementoCatalogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String tipo;
    private String marca;
    private String presentacion;

    @ManyToOne
    @JoinColumn(name = "unidad_presentacion_id")
    private UnidadMedida unidadPresentacion;

    private Boolean activo = true;

    @Column(length = 2000)
    private String descripcion;

    @Column(length = 2000)
    private String beneficios;

    @Column(length = 2000)
    private String recomendaciones;

    public SuplementoCatalogo() {}

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMarca() {
        return marca;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public UnidadMedida getUnidadPresentacion() {
        return unidadPresentacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setNombre(String v) {
        nombre = v;
    }

    public void setTipo(String v) {
        tipo = v;
    }

    public void setMarca(String v) {
        marca = v;
    }

    public void setPresentacion(String v) {
        presentacion = v;
    }

    public void setUnidadPresentacion(UnidadMedida v) {
        unidadPresentacion = v;
    }

    public void setActivo(Boolean v) {
        activo = v;
    }

    public void setDescripcion(String v) {
        descripcion = v;
    }

    public void setBeneficios(String v) {
        beneficios = v;
    }

    public void setRecomendaciones(String v) {
        recomendaciones = v;
    }
}
