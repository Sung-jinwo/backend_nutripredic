package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.suplemento.dto.ComponenteComposicionSuplementoRequest;
import com.backend.nutri_predic.suplemento.dto.ComposicionSuplementoRequest;
import com.backend.nutri_predic.suplemento.entity.*;
import com.backend.nutri_predic.suplemento.repository.*;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComposicionSuplementoService {
    private final SuplementoCatalogoRepository suplementos;
    private final ComposicionSuplementoRepository composiciones;
    private final ComponenteComposicionSuplementoRepository componentes;
    private final UnidadMedidaRepository unidades;

    public ComposicionSuplementoService(
            SuplementoCatalogoRepository suplementos,
            ComposicionSuplementoRepository composiciones,
            ComponenteComposicionSuplementoRepository componentes,
            UnidadMedidaRepository unidades) {
        this.suplementos = suplementos;
        this.composiciones = composiciones;
        this.componentes = componentes;
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public List<ComposicionSuplemento> listarPorSuplemento(Long suplementoId) {
        suplemento(suplementoId);
        return composiciones.findBySuplementoIdOrderByVersionDesc(suplementoId);
    }

    @Transactional(readOnly = true)
    public Optional<ComposicionSuplemento> obtenerActiva(Long suplementoId, LocalDate fecha) {
        suplemento(suplementoId);
        return composiciones.activas(suplementoId, fecha).stream().findFirst();
    }

    @Transactional
    public ComposicionSuplemento crear(Long suplementoId, ComposicionSuplementoRequest r) {
        if (r.fechaHasta() != null && r.fechaHasta().isBefore(r.fechaDesde()))
            throw new BusinessException("fechaHasta no puede ser anterior a fechaDesde");
        var c = new ComposicionSuplemento();
        c.setSuplemento(suplemento(suplementoId));
        c.setVersion(
                composiciones
                        .findTopBySuplementoIdOrderByVersionDesc(suplementoId)
                        .map(x -> x.getVersion() + 1)
                        .orElse(1));
        c.setCantidadPorcionReferencia(r.cantidadPorcionReferencia());
        c.setEnergiaKcalPorcion(r.energiaKcalPorcion());
        c.setUnidadPorcion(unidad(r.unidadPorcionCodigo()));
        c.setFechaDesde(r.fechaDesde());
        c.setFechaHasta(r.fechaHasta());
        c.setActivo(r.activo() == null || r.activo());
        c.setFuenteDatos(r.fuenteDatos());
        return composiciones.save(c);
    }

    @Transactional(readOnly = true)
    public List<ComponenteComposicionSuplemento> listarComponentes(Long composicionId) {
        composicion(composicionId);
        return componentes.findByComposicionIdOrderByIdAsc(composicionId);
    }

    @Transactional
    public ComponenteComposicionSuplemento agregarComponente(
            Long composicionId, ComponenteComposicionSuplementoRequest r) {
        if (r.tipo() == TipoComponenteSuplemento.OTRO
                && (r.nombreOtro() == null || r.nombreOtro().isBlank()))
            throw new BusinessException("OTRO requiere nombre adicional");
        var x = new ComponenteComposicionSuplemento();
        x.setComposicion(composicion(composicionId));
        x.setTipo(r.tipo());
        x.setNombreOtro(r.nombreOtro() == null ? null : r.nombreOtro().trim());
        x.setCantidad(r.cantidad());
        x.setUnidad(unidad(r.unidadCodigo()));
        return componentes.save(x);
    }

    private SuplementoCatalogo suplemento(Long id) {
        return suplementos
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suplemento"));
    }

    private ComposicionSuplemento composicion(Long id) {
        return composiciones
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Composición"));
    }

    private com.backend.nutri_predic.unidad.entity.UnidadMedida unidad(String codigo) {
        return unidades.findByCodigoIgnoreCaseAndActivaTrue(codigo.trim())
                .orElseThrow(() -> new BusinessException("Unidad no válida: " + codigo));
    }
}
