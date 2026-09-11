package com.backend.nutri_predic.requerimientonutricional.service;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.actividadfisica.repository.*;
import com.backend.nutri_predic.requerimientonutricional.dto.*;
import com.backend.nutri_predic.requerimientonutricional.entity.*;
import com.backend.nutri_predic.requerimientonutricional.repository.RequerimientoNutricionalReglaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Selecciona requerimientos referenciados; no calcula diagnóstico ni tratamiento. */
@Service
public class RequerimientoNutricionalService {
    private final RequerimientoNutricionalReglaRepository reglas;
    private final EvaluacionActividadFisicaRepository evaluacionesActividad;
    private final MapeoCategoriaActividadRepository mapeosActividad;
    public RequerimientoNutricionalService(RequerimientoNutricionalReglaRepository reglas, EvaluacionActividadFisicaRepository evaluacionesActividad, MapeoCategoriaActividadRepository mapeosActividad) { this.reglas = reglas; this.evaluacionesActividad = evaluacionesActividad; this.mapeosActividad = mapeosActividad; }
    @Transactional(readOnly = true)
    public RequerimientoNutricionalResponse resolver(Cliente cliente) { return resolver(cliente, LocalDate.now()); }
    @Transactional(readOnly = true)
    public RequerimientoNutricionalResponse resolver(Cliente cliente, LocalDate fecha) {
        String incompleto = perfilIncompleto(cliente);
        if (incompleto != null) return RequerimientoNutricionalResponse.noDisponible(incompleto);
        return reglas.findAllByOrderByIdAsc().stream().filter(r -> aplica(r, cliente, fecha))
                .max(Comparator.comparingInt(this::especificidad))
                .map(r -> new RequerimientoNutricionalResponse("DISPONIBLE", r.getFuenteReferencia(), r.getVersionReferencia(),
                        r.getKcalObjetivo(), r.getProteinaObjetivoG(), r.getCarbohidratosObjetivoG(), r.getGrasasObjetivoG(),
                        "REQUERIMIENTO_ESTIMADO_REFERENCIADO", null))
                .orElseGet(() -> RequerimientoNutricionalResponse.noDisponible("No existe una regla activa, validada y vigente para el perfil"));
    }
    @Transactional
    public Long crearRegla(RequerimientoNutricionalReglaRequest r) {
        if (r.edadMinima() > r.edadMaxima() || mayor(r.pesoMinimoKg(), r.pesoMaximoKg()) || mayor(r.alturaMinimaCm(), r.alturaMaximaCm())
                || mayor(r.diasEntrenamientoMinimo(), r.diasEntrenamientoMaximo()) || mayor(r.duracionSesionMinima(), r.duracionSesionMaxima())
                || r.vigenteDesde() != null && r.vigenteHasta() != null && r.vigenteDesde().isAfter(r.vigenteHasta()))
            throw new IllegalArgumentException("Los rangos y la vigencia de la regla son inválidos");
        if (r.estado() == EstadoReglaRequerimientoNutricional.ACTIVA && !r.validada())
            throw new IllegalArgumentException("Una regla ACTIVA debe estar validada");
        if (!r.realizaActividadFisica() && (r.diasEntrenamientoMinimo() != null || r.diasEntrenamientoMaximo() != null || r.tipoActividadFisica() != null || r.duracionSesionMinima() != null || r.duracionSesionMaxima() != null))
            throw new IllegalArgumentException("Una regla sin actividad no admite condiciones de entrenamiento");
        var x = new RequerimientoNutricionalRegla();
        x.setEstado(r.estado()); x.setValidada(r.validada()); x.setFuenteReferencia(r.fuenteReferencia().trim()); x.setVersionReferencia(r.versionReferencia().trim());
        x.setVigenteDesde(r.vigenteDesde()); x.setVigenteHasta(r.vigenteHasta()); x.setEdadMinima(r.edadMinima()); x.setEdadMaxima(r.edadMaxima());
        x.setPesoMinimoKg(r.pesoMinimoKg()); x.setPesoMaximoKg(r.pesoMaximoKg()); x.setAlturaMinimaCm(r.alturaMinimaCm()); x.setAlturaMaximaCm(r.alturaMaximaCm());
        x.setRealizaActividadFisica(r.realizaActividadFisica()); x.setDiasEntrenamientoMinimo(r.diasEntrenamientoMinimo()); x.setDiasEntrenamientoMaximo(r.diasEntrenamientoMaximo());
        x.setTipoActividadFisica(texto(r.tipoActividadFisica())); x.setDuracionSesionMinima(r.duracionSesionMinima()); x.setDuracionSesionMaxima(r.duracionSesionMaxima());
        x.setObjetivoEnergetico(r.objetivoEnergetico()); x.setKcalObjetivo(r.kcalObjetivo()); x.setProteinaObjetivoG(r.proteinaObjetivoG()); x.setCarbohidratosObjetivoG(r.carbohidratosObjetivoG()); x.setGrasasObjetivoG(r.grasasObjetivoG());
        return reglas.save(x).getId();
    }
    private boolean aplica(RequerimientoNutricionalRegla r, Cliente c, LocalDate fecha) {
        return r.getEstado() == EstadoReglaRequerimientoNutricional.ACTIVA && Boolean.TRUE.equals(r.getValidada())
                && (r.getVigenteDesde() == null || !r.getVigenteDesde().isAfter(fecha)) && (r.getVigenteHasta() == null || !r.getVigenteHasta().isBefore(fecha))
                && entre(c.getEdad(), r.getEdadMinima(), r.getEdadMaxima()) && entre(c.getPesoKg(), r.getPesoMinimoKg(), r.getPesoMaximoKg()) && entre(c.getAlturaCm(), r.getAlturaMinimaCm(), r.getAlturaMaximaCm())
                && c.getRealizaActividadFisica().equals(r.getRealizaActividadFisica()) && c.getObjetivoEnergetico() == r.getObjetivoEnergetico()
                && entre(c.getDiasEntrenamientoSemana(), r.getDiasEntrenamientoMinimo(), r.getDiasEntrenamientoMaximo()) && entre(c.getDuracionPromedioSesionMinutos(), r.getDuracionSesionMinima(), r.getDuracionSesionMaxima())
                && (r.getTipoRegla() != TipoReglaRequerimientoNutricional.FORMULA_ENERGETICA || actividadCompatible(r, c.getId(), fecha));
    }
    private boolean actividadCompatible(RequerimientoNutricionalRegla regla, Long clienteId, LocalDate fecha) {
        return evaluacionesActividad.findByClienteIdOrderByFechaEvaluacionDesc(clienteId).stream()
                .filter(e -> Boolean.TRUE.equals(e.getValidada()))
                .filter(e -> (e.getVigenciaDesde() == null || !e.getVigenciaDesde().isAfter(fecha)) && (e.getVigenciaHasta() == null || !e.getVigenciaHasta().isBefore(fecha)))
                .anyMatch(e -> mapeosActividad.findAllByOrderByIdAsc().stream()
                        .anyMatch(m -> Boolean.TRUE.equals(m.getValidada())
                                && m.getInstrumentoCodigo().equalsIgnoreCase(e.getInstrumentoCodigo())
                                && m.getInstrumentoVersion().equalsIgnoreCase(e.getInstrumentoVersion())
                                && m.getResultadoOrigen().equalsIgnoreCase(e.getNivelResultado())
                                && (m.getVigenciaDesde() == null || !m.getVigenciaDesde().isAfter(fecha)) && (m.getVigenciaHasta() == null || !m.getVigenciaHasta().isBefore(fecha))
                                && m.getFuenteReferencia().equalsIgnoreCase(regla.getFuenteReferencia())
                                && m.getVersionReferencia().equalsIgnoreCase(regla.getVersionReferencia())
                                && m.getCategoriaDestino().equalsIgnoreCase(regla.getTipoActividadFisica())));
    }
    private String perfilIncompleto(Cliente c) {
        if (c.getEdad() == null || c.getPesoKg() == null || c.getAlturaCm() == null || c.getSexo() == null) return "Faltan edad, sexo, pesoKg o alturaCm";
        if (c.getRealizaActividadFisica() == null || c.getObjetivoEnergetico() == null) return "Falta actividad física u objetivo energético";
        if (c.getRealizaActividadFisica() && (c.getDiasEntrenamientoSemana() == null || c.getTipoActividadFisica() == null || c.getDuracionPromedioSesionMinutos() == null)) return "Faltan datos de entrenamiento";
        return null;
    }
    private boolean entre(Integer x, Integer min, Integer max) { return x != null && (min == null || x >= min) && (max == null || x <= max); }
    private boolean entre(BigDecimal x, BigDecimal min, BigDecimal max) { return x != null && (min == null || x.compareTo(min) >= 0) && (max == null || x.compareTo(max) <= 0); }
    private boolean mayor(Comparable a, Comparable b) { return a != null && b != null && a.compareTo(b) > 0; }
    private int especificidad(RequerimientoNutricionalRegla r) { return (r.getTipoActividadFisica() == null ? 0 : 1) + (r.getPesoMinimoKg() == null && r.getPesoMaximoKg() == null ? 0 : 1) + (r.getAlturaMinimaCm() == null && r.getAlturaMaximaCm() == null ? 0 : 1) + (r.getDiasEntrenamientoMinimo() == null && r.getDiasEntrenamientoMaximo() == null ? 0 : 1) + (r.getDuracionSesionMinima() == null && r.getDuracionSesionMaxima() == null ? 0 : 1); }
    private String texto(String x) { return x == null || x.isBlank() ? null : x.trim(); }
}
