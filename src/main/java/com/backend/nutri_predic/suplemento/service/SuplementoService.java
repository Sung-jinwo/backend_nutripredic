package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.entity.*;
import com.backend.nutri_predic.suplemento.repository.*;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuplementoService {
    private final SuplementoCatalogoRepository catalog;
    private final SuplementoClienteRepository assigned;
    private final HistorialSuplementoClienteRepository historial;
    private final UnidadMedidaRepository unidades;
    private final AccessService access;

    public SuplementoService(
            SuplementoCatalogoRepository catalog,
            SuplementoClienteRepository assigned,
            HistorialSuplementoClienteRepository historial,
            UnidadMedidaRepository unidades,
            AccessService access) {
        this.catalog = catalog;
        this.assigned = assigned;
        this.historial = historial;
        this.unidades = unidades;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public List<SuplementoCatalogoResponse> catalog() {
        return catalog.findAll().stream().map(SuplementoCatalogoResponse::from).toList();
    }

    @Transactional
    public SuplementoCatalogoResponse saveCatalog(Long id, SuplementoCatalogoRequest r) {
        if (id == null && catalog.existsByNombreIgnoreCase(r.nombre().trim()))
            throw new ConflictException("El suplemento ya existe en el catálogo");
        var s =
                id == null
                        ? new SuplementoCatalogo()
                        : catalog.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Suplemento"));
        s.setNombre(r.nombre().trim());
        s.setTipo(r.tipo().trim());
        s.setDescripcion(trimToNull(r.descripcion()));
        s.setBeneficios(trimToNull(r.beneficios()));
        s.setRecomendaciones(trimToNull(r.recomendaciones()));
        s.setMarca(trimToNull(r.marca()));
        s.setPresentacion(trimToNull(r.presentacion()));
        s.setUnidadPresentacion(
                r.unidadPresentacionCodigo() == null ? null : unidad(r.unidadPresentacionCodigo()));
        if (r.activo() != null) s.setActivo(r.activo());
        return SuplementoCatalogoResponse.from(catalog.save(s));
    }

    @Transactional
    public void deleteCatalog(Long id) {
        var s = catalog.findById(id).orElseThrow(() -> new ResourceNotFoundException("Suplemento"));
        s.setActivo(false);
        catalog.save(s);
    }

    @Transactional(readOnly = true)
    public List<SuplementoClienteResponse> list(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        return assigned.findByClienteId(clienteId).stream()
                .map(SuplementoClienteResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuplementoClienteResponse> habituales(
            Long clienteId, LocalDate fecha, Authentication auth) {
        access.client(clienteId, auth);
        return assigned
                .findByClienteIdAndActivoTrueOrderBySuplementoNombreAscIdAsc(clienteId)
                .stream()
                .filter(
                        s ->
                                s.getFechaInicio() != null
                                        && !s.getFechaInicio().isAfter(fecha)
                                        && (s.getFechaFin() == null
                                                || !s.getFechaFin().isBefore(fecha)))
                .map(SuplementoClienteResponse::from)
                .toList();
    }

    @Transactional
    public SuplementoClienteResponse assign(
            Long clienteId, SuplementoAsignacionRequest r, Authentication auth) {
        var cliente = access.client(clienteId, auth);
        var suplemento = resolverSuplemento(r.suplementoId(), r.nombreSuplemento(), r.unidadCodigo());
        if (assigned.findByClienteIdAndSuplementoId(clienteId, suplemento.getId()).isPresent())
            throw new ConflictException("El suplemento ya está asignado al cliente");
        validateDates(r.fechaInicio(), r.fechaFin());
        var asignacion = new SuplementoCliente();
        asignacion.setCliente(cliente);
        asignacion.setSuplemento(suplemento);
        asignacion.setNombreDeclarado(
                r.nombreSuplemento() == null || r.nombreSuplemento().isBlank()
                        ? suplemento.getNombre()
                        : r.nombreSuplemento().trim());
        apply(
                asignacion,
                r.cantidad(),
                r.unidad(),
                r.frecuencia(),
                r.tiempoUso(),
                r.activo(),
                r.fechaInicio(),
                r.fechaFin(),
                r.cantidadPorToma(),
                r.unidadCodigo(),
                r.tomasPorPeriodo(),
                r.periodoFrecuencia(), r.componentesDeclarados(), r.energiaKcalPorToma(), r.proteinaGPorToma(), r.carbohidratosGPorToma(), r.grasasGPorToma(), r.creatinaGPorToma(), r.cafeinaMgPorToma(), r.sodioMgPorToma());
        asignacion = assigned.save(asignacion);
        historial.save(new HistorialSuplementoCliente(asignacion));
        return SuplementoClienteResponse.from(asignacion);
    }

    @Transactional
    public SuplementoClienteResponse update(
            Long clienteId,
            Long suplementoId,
            SuplementoActualizacionRequest r,
            Authentication auth) {
        access.client(clienteId, auth);
        validateDates(r.fechaInicio(), r.fechaFin());
        var asignacion =
                assigned.findByClienteIdAndSuplementoId(clienteId, suplementoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Asignación"));
        asignacion.setNombreDeclarado(
                r.nombreSuplemento() == null || r.nombreSuplemento().isBlank()
                        ? asignacion.getNombreDeclarado()
                        : r.nombreSuplemento().trim());
        apply(
                asignacion,
                r.cantidad(),
                r.unidad(),
                r.frecuencia(),
                r.tiempoUso(),
                r.activo(),
                r.fechaInicio(),
                r.fechaFin(),
                r.cantidadPorToma(),
                r.unidadCodigo(),
                r.tomasPorPeriodo(),
                r.periodoFrecuencia(), r.componentesDeclarados(), r.energiaKcalPorToma(), r.proteinaGPorToma(), r.carbohidratosGPorToma(), r.grasasGPorToma(), r.creatinaGPorToma(), r.cafeinaMgPorToma(), r.sodioMgPorToma());
        asignacion = assigned.save(asignacion);
        historial.save(new HistorialSuplementoCliente(asignacion));
        return SuplementoClienteResponse.from(asignacion);
    }

    @Transactional
    public void remove(Long clienteId, Long suplementoId, Authentication auth) {
        access.client(clienteId, auth);
        var asignacion =
                assigned.findByClienteIdAndSuplementoId(clienteId, suplementoId)
                        .orElseThrow(() -> new ResourceNotFoundException("Asignación"));
        asignacion.setActivo(false);
        if (asignacion.getFechaFin() == null || asignacion.getFechaFin().isAfter(LocalDate.now()))
            asignacion.setFechaFin(LocalDate.now());
        assigned.save(asignacion);
        historial.save(new HistorialSuplementoCliente(asignacion));
    }

    private void apply(
            SuplementoCliente a,
            Double cantidad,
            String unidadLegacy,
            String frecuencia,
            String tiempoUso,
            Boolean activo,
            LocalDate inicio,
            LocalDate fin,
            BigDecimal cantidadPorToma,
            String unidadCodigo,
            Integer tomas,
            PeriodoFrecuencia periodo, String componentesDeclarados, BigDecimal energiaKcalPorToma,
            BigDecimal proteinaGPorToma, BigDecimal carbohidratosGPorToma, BigDecimal grasasGPorToma,
            BigDecimal creatinaGPorToma, BigDecimal cafeinaMgPorToma, BigDecimal sodioMgPorToma) {
        a.setCantidad(cantidad);
        a.setUnidad(unidadLegacy.trim());
        a.setFrecuencia(trimToNull(frecuencia));
        a.setTiempoUso(trimToNull(tiempoUso));
        a.setActivo(activo);
        a.setFechaInicio(inicio);
        a.setFechaFin(fin);
        boolean porcionParcial = (cantidadPorToma == null) != (unidadCodigo == null || unidadCodigo.isBlank());
        if (porcionParcial)
            throw new BusinessException("La cantidad por toma y su unidad deben enviarse juntas");
        boolean frecuenciaParcial = (tomas == null) != (periodo == null);
        if (frecuenciaParcial)
            throw new BusinessException("Las tomas y el periodo deben enviarse juntos");
        a.setCantidadPorToma(cantidadPorToma);
        a.setUnidadMedida(cantidadPorToma == null ? null : unidad(unidadCodigo));
        a.setTomasPorPeriodo(tomas);
        a.setPeriodoFrecuencia(periodo);
        a.setComponentesDeclarados(trimToNull(componentesDeclarados));
        a.setEnergiaKcalPorToma(energiaKcalPorToma);
        a.setProteinaGPorToma(proteinaGPorToma);
        a.setCarbohidratosGPorToma(carbohidratosGPorToma);
        a.setGrasasGPorToma(grasasGPorToma);
        a.setCreatinaGPorToma(creatinaGPorToma);
        a.setCafeinaMgPorToma(cafeinaMgPorToma);
        a.setSodioMgPorToma(sodioMgPorToma);
    }

    private SuplementoCatalogo resolverSuplemento(
            Long suplementoId, String nombreSuplemento, String unidadCodigo) {
        if (suplementoId != null)
            return catalog.findById(suplementoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Suplemento"));
        var nombre = nombreSuplemento.trim();
        return catalog.findByNombreIgnoreCase(nombre).orElseGet(() -> {
            var suplemento = new SuplementoCatalogo();
            suplemento.setNombre(nombre);
            suplemento.setTipo("PERSONALIZADO");
            suplemento.setUnidadPresentacion(
                    unidadCodigo == null || unidadCodigo.isBlank() ? null : unidad(unidadCodigo));
            suplemento.setActivo(true);
            return catalog.save(suplemento);
        });
    }

    private void validateDates(LocalDate inicio, LocalDate fin) {
        if (fin != null && fin.isBefore(inicio))
            throw new BusinessException("fechaFin no puede ser anterior a fechaInicio");
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private UnidadMedida unidad(String codigo) {
        return unidades.findByCodigoIgnoreCaseAndActivaTrue(codigo.trim())
                .orElseThrow(() -> new BusinessException("Unidad no válida: " + codigo));
    }

}
