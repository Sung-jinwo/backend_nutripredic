package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.*;
import com.backend.nutri_predic.alimentacion.repository.*;
import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquivalenciaUnidadAlimentoService {
    private final EquivalenciaUnidadAlimentoRepository r;
    private final AlimentoCatalogoRepository a;
    private final UnidadMedidaRepository u;

    public EquivalenciaUnidadAlimentoService(
            EquivalenciaUnidadAlimentoRepository r,
            AlimentoCatalogoRepository a,
            UnidadMedidaRepository u) {
        this.r = r;
        this.a = a;
        this.u = u;
    }

    @Transactional
    public EquivalenciaUnidadAlimentoResponse crear(Long id, EquivalenciaUnidadAlimentoRequest x) {
        if (x.fechaHasta() != null && x.fechaHasta().isBefore(x.fechaDesde()))
            throw new BusinessException("fechaHasta no puede ser anterior a fechaDesde");
        var e = new EquivalenciaUnidadAlimento();
        e.setAlimento(a.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alimento")));
        e.setVersion(
                r.findTopByAlimentoIdOrderByVersionDesc(id).map(v -> v.getVersion() + 1).orElse(1));
        e.setCantidadOrigen(x.cantidadOrigen());
        e.setCantidadDestino(x.cantidadDestino());
        e.setUnidadOrigen(unidad(x.unidadOrigenCodigo()));
        e.setUnidadDestino(unidad(x.unidadDestinoCodigo()));
        e.setFuenteDatos(x.fuenteDatos());
        e.setFechaDesde(x.fechaDesde());
        e.setFechaHasta(x.fechaHasta());
        e.setActivo(x.activo() == null || x.activo());
        return EquivalenciaUnidadAlimentoResponse.from(r.save(e));
    }

    @Transactional(readOnly = true)
    public List<EquivalenciaUnidadAlimentoResponse> listar(Long id) {
        a.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        return r.findByAlimentoIdOrderByVersionDesc(id).stream()
                .map(EquivalenciaUnidadAlimentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquivalenciaUnidadAlimentoResponse> listarActivas(Long id, LocalDate fecha) {
        a.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        return r.activasFecha(id, fecha).stream()
                .map(EquivalenciaUnidadAlimentoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<EquivalenciaUnidadAlimento> resolver(
            Long alimento, String origen, String destino, LocalDate fecha) {
        if (origen != null && destino != null && origen.equalsIgnoreCase(destino)) return Optional.empty();
        var xs = r.activas(alimento, origen, destino, fecha);
        // Una conversión ambigua no debe seleccionar arbitrariamente una equivalencia.
        return xs.size() == 1 ? Optional.of(xs.getFirst()) : Optional.empty();
    }

    private com.backend.nutri_predic.unidad.entity.UnidadMedida unidad(String c) {
        return u.findByCodigoIgnoreCaseAndActivaTrue(c.trim())
                .orElseThrow(() -> new BusinessException("Unidad no válida: " + c));
    }
}
