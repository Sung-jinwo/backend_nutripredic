package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.*;
import com.backend.nutri_predic.alimentacion.repository.*;
import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComposicionNutricionalAlimentoService {
    private final AlimentoCatalogoRepository alimentos;
    private final ComposicionNutricionalAlimentoRepository composiciones;
    private final UnidadMedidaRepository unidades;

    public ComposicionNutricionalAlimentoService(
            AlimentoCatalogoRepository a,
            ComposicionNutricionalAlimentoRepository c,
            UnidadMedidaRepository u) {
        alimentos = a;
        composiciones = c;
        unidades = u;
    }

    @Transactional(readOnly = true)
    public List<ComposicionNutricionalAlimentoResponse> listar(Long id) {
        validar(id);
        return composiciones.findByAlimentoIdOrderByVersionDesc(id).stream()
                .map(ComposicionNutricionalAlimentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ComposicionNutricionalAlimentoResponse activa(Long id, LocalDate fecha) {
        return ComposicionNutricionalAlimentoResponse.from(
                resolverActiva(id, fecha)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Composición nutricional activa")));
    }

    @Transactional
    public ComposicionNutricionalAlimentoResponse crear(
            Long id, ComposicionNutricionalAlimentoRequest r) {
        if (r.fechaHasta() != null && r.fechaHasta().isBefore(r.fechaDesde()))
            throw new BusinessException("fechaHasta no puede ser anterior a fechaDesde");
        var c = new ComposicionNutricionalAlimento();
        c.setAlimento(validar(id));
        c.setVersion(
                composiciones
                        .findTopByAlimentoIdOrderByVersionDesc(id)
                        .map(x -> x.getVersion() + 1)
                        .orElse(1));
        c.setCantidadReferencia(r.cantidadReferencia());
        c.setUnidadReferencia(
                unidades.findByCodigoIgnoreCaseAndActivaTrue(r.unidadReferenciaCodigo().trim())
                        .orElseThrow(
                                () ->
                                        new BusinessException(
                                                "Unidad no válida: "
                                                        + r.unidadReferenciaCodigo())));
        c.setKcal(r.kcal());
        c.setProteinaG(r.proteinaG());
        c.setCarbohidratosG(r.carbohidratosG());
        c.setGrasasG(r.grasasG());
        c.setFibraG(r.fibraG());
        c.setAzucarG(r.azucarG());
        c.setSodioMg(r.sodioMg());
        c.setFuenteDatos(t(r.fuenteDatos()));
        c.setFechaDesde(r.fechaDesde());
        c.setFechaHasta(r.fechaHasta());
        c.setActivo(r.activo() == null || r.activo());
        return ComposicionNutricionalAlimentoResponse.from(composiciones.save(c));
    }

    @Transactional(readOnly = true)
    public Optional<ComposicionNutricionalAlimento> resolverActiva(Long id, LocalDate fecha) {
        return composiciones.findActivasAplicables(id, fecha).stream().findFirst();
    }

    private AlimentoCatalogo validar(Long id) {
        return alimentos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alimento"));
    }

    private String t(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
