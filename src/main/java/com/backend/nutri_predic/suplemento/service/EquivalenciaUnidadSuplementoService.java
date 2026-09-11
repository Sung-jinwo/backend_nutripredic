package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.suplemento.dto.EquivalenciaUnidadSuplementoRequest;
import com.backend.nutri_predic.suplemento.entity.*;
import com.backend.nutri_predic.suplemento.repository.*;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
public class EquivalenciaUnidadSuplementoService {
    private final EquivalenciaUnidadSuplementoRepository equivalencias;
    private final SuplementoCatalogoRepository suplementos;
    private final UnidadMedidaRepository unidades;

    public EquivalenciaUnidadSuplementoService(
            EquivalenciaUnidadSuplementoRepository e,
            SuplementoCatalogoRepository s,
            UnidadMedidaRepository u) {
        equivalencias = e;
        suplementos = s;
        unidades = u;
    }

    @Transactional(readOnly = true)
    public List<EquivalenciaUnidadSuplemento> listarPorSuplemento(Long id) {
        suplemento(id);
        return equivalencias.findBySuplementoIdOrderByVersionDesc(id);
    }

    @Transactional(readOnly = true)
    public List<EquivalenciaUnidadSuplemento> listarActivas(Long id, LocalDate fecha) {
        return listarPorSuplemento(id).stream()
                .filter(
                        e ->
                                Boolean.TRUE.equals(e.getActivo())
                                        && !e.getFechaDesde().isAfter(fecha)
                                        && (e.getFechaHasta() == null
                                                || !e.getFechaHasta().isBefore(fecha)))
                .toList();
    }

    @Transactional
    public EquivalenciaUnidadSuplemento crear(Long id, EquivalenciaUnidadSuplementoRequest r) {
        if (r.fechaHasta() != null && r.fechaHasta().isBefore(r.fechaDesde()))
            throw new BusinessException("fechaHasta no puede ser anterior a fechaDesde");
        var e = new EquivalenciaUnidadSuplemento();
        e.setSuplemento(suplemento(id));
        e.setVersion(
                equivalencias
                        .findTopBySuplementoIdOrderByVersionDesc(id)
                        .map(x -> x.getVersion() + 1)
                        .orElse(1));
        e.setCantidadOrigen(r.cantidadOrigen());
        e.setUnidadOrigen(unidad(r.unidadOrigenCodigo()));
        e.setCantidadDestino(r.cantidadDestino());
        e.setUnidadDestino(unidad(r.unidadDestinoCodigo()));
        e.setFechaDesde(r.fechaDesde());
        e.setFechaHasta(r.fechaHasta());
        e.setActivo(r.activo() == null || r.activo());
        return equivalencias.save(e);
    }

    private SuplementoCatalogo suplemento(Long id) {
        return suplementos
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suplemento"));
    }

    private com.backend.nutri_predic.unidad.entity.UnidadMedida unidad(String c) {
        return unidades.findByCodigoIgnoreCaseAndActivaTrue(c.trim())
                .orElseThrow(() -> new BusinessException("Unidad no válida: " + c));
    }
}
