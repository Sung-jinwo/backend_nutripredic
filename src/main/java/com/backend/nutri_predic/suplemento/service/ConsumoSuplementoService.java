package com.backend.nutri_predic.suplemento.service;

import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import com.backend.nutri_predic.suplemento.dto.*;
import com.backend.nutri_predic.suplemento.entity.*;
import com.backend.nutri_predic.suplemento.repository.*;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumoSuplementoService {
    private final RegistroConsumoSuplementoRepository consumos;
    private final RegistroHabitoRepository habitos;
    private final SuplementoClienteRepository habituales;
    private final UnidadMedidaRepository unidades;
    private final AccessService access;
    private final ComposicionSuplementoRepository composiciones;
    private final EquivalenciaUnidadSuplementoRepository equivalencias;
    private final PlanDiarioService planes;

    public ConsumoSuplementoService(
            RegistroConsumoSuplementoRepository c,
            RegistroHabitoRepository h,
            SuplementoClienteRepository s,
            UnidadMedidaRepository u,
            AccessService a,
            ComposicionSuplementoRepository co,
            EquivalenciaUnidadSuplementoRepository e,
            PlanDiarioService planes) {
        consumos = c;
        habitos = h;
        habituales = s;
        unidades = u;
        access = a;
        composiciones = co;
        equivalencias = e;
        this.planes = planes;
    }

    @Transactional(readOnly = true)
    public List<RegistroConsumoSuplementoResponse> listar(Long habitoId, Authentication auth) {
        var h = habito(habitoId, auth);
        return consumos.findByRegistroHabitoIdOrderByIdAsc(h.getId()).stream()
                .map(RegistroConsumoSuplementoResponse::from)
                .toList();
    }

    @Transactional
    public RegistroConsumoSuplementoResponse crear(
            Long habitoId, RegistroConsumoSuplementoRequest r, Authentication auth) {
        var h = habito(habitoId, auth);
        if (consumos.existsByRegistroHabitoIdAndSuplementoClienteId(
                habitoId, r.suplementoClienteId()))
            throw new ConflictException("El suplemento ya fue registrado en este hábito");
        var c = new RegistroConsumoSuplemento();
        aplicar(c, h, r);
        c = consumos.save(c);
        sincronizarLegacy(h);
        planes.recalcularSiExiste(h.getCliente().getId(), h.getFecha());
        return RegistroConsumoSuplementoResponse.from(c);
    }

    /**
     * Compatibility entry point for the historical client/date API. It resolves the daily habit
     * first and delegates persistence to the same consumption flow used by the canonical endpoint.
     */
    @Transactional
    public RegistroConsumoSuplementoResponse crearPorClienteYFecha(
            Long clienteId, RegistroConsumoSuplementoClienteRequest r, Authentication auth) {
        access.client(clienteId, auth);
        if (!"CONSUMIDO".equalsIgnoreCase(r.estado().trim()))
            throw new BusinessException("El estado de consumo debe ser CONSUMIDO");
        var h = habitos.findByClienteIdAndFecha(clienteId, r.fecha())
                .orElseThrow(() -> new ResourceNotFoundException("Hábito para la fecha indicada"));
        return crear(
                h.getId(),
                new RegistroConsumoSuplementoRequest(
                        r.suplementoClienteId(), r.cantidad(), normalizarCodigoUnidad(r.unidad()), null, null),
                auth);
    }

    @Transactional
    public RegistroConsumoSuplementoResponse actualizar(
            Long habitoId, Long id, RegistroConsumoSuplementoRequest r, Authentication auth) {
        var h = habito(habitoId, auth);
        var c =
                consumos.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Consumo de suplemento"));
        if (!c.getRegistroHabito().getId().equals(habitoId))
            throw new ResourceNotFoundException("Consumo de suplemento");
        if (!c.getSuplementoCliente().getId().equals(r.suplementoClienteId())
                && consumos.existsByRegistroHabitoIdAndSuplementoClienteId(
                        habitoId, r.suplementoClienteId()))
            throw new ConflictException("El suplemento ya fue registrado en este hábito");
        aplicar(c, h, r);
        var guardado = consumos.save(c);
        planes.recalcularSiExiste(h.getCliente().getId(), h.getFecha());
        return RegistroConsumoSuplementoResponse.from(guardado);
    }

    @Transactional
    public void eliminar(Long habitoId, Long id, Authentication auth) {
        var h = habito(habitoId, auth);
        var c =
                consumos.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Consumo de suplemento"));
        if (!c.getRegistroHabito().getId().equals(habitoId))
            throw new ResourceNotFoundException("Consumo de suplemento");
        consumos.delete(c);
        consumos.flush();
        sincronizarLegacy(h);
        planes.recalcularSiExiste(h.getCliente().getId(), h.getFecha());
    }

    private void aplicar(
            RegistroConsumoSuplemento c, RegistroHabito h, RegistroConsumoSuplementoRequest r) {
        var s =
                habituales
                        .findById(r.suplementoClienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Suplemento habitual"));
        if (!s.getCliente().getId().equals(h.getCliente().getId()))
            throw new BusinessException(
                    "El suplemento habitual no pertenece al cliente del hábito");
        if (s.getFechaInicio() == null
                || s.getFechaInicio().isAfter(h.getFecha())
                || (s.getFechaFin() != null && s.getFechaFin().isBefore(h.getFecha())))
            throw new BusinessException(
                    "El suplemento habitual no era aplicable en la fecha del hábito");
        var u = unidad(r.unidadCodigo());
        c.setRegistroHabito(h);
        c.setSuplementoCliente(s);
        c.setCantidadConsumida(r.cantidadConsumida());
        c.setUnidad(u);
        c.setNumeroTomas(r.numeroTomas());
        c.setComposicionSuplemento(null);
        c.setEquivalenciaUnidad(null);
        var xs = composiciones.activas(s.getSuplemento().getId(), h.getFecha());
        if (xs.size() == 1) {
            var comp = xs.getFirst();
            c.setComposicionSuplemento(comp);
            if (!u.getCodigo().equalsIgnoreCase(comp.getUnidadPorcion().getCodigo())) {
                var es =
                        equivalencias.activas(
                                s.getSuplemento().getId(),
                                u.getCodigo(),
                                comp.getUnidadPorcion().getCodigo(),
                                h.getFecha());
                if (es.size() == 1) c.setEquivalenciaUnidad(es.getFirst());
            }
        }
        c.setObservacion(
                r.observacion() == null || r.observacion().isBlank()
                        ? null
                        : r.observacion().trim());
    }

    private RegistroHabito habito(Long id, Authentication auth) {
        var h = habitos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hábito"));
        access.client(h.getCliente().getId(), auth);
        return h;
    }

    private UnidadMedida unidad(String c) {
        return unidades.findByCodigoIgnoreCaseAndActivaTrue(c.trim())
                .orElseThrow(() -> new BusinessException("Unidad no válida: " + c));
    }

    private String normalizarCodigoUnidad(String codigo) {
        String normalizado = codigo.trim();
        return "GRAMOS".equalsIgnoreCase(normalizado) ? "G" : normalizado;
    }

    private void sincronizarLegacy(RegistroHabito h) {
        h.setConsumeSuplementos(consumos.existsByRegistroHabitoId(h.getId()));
        habitos.save(h);
    }
}
