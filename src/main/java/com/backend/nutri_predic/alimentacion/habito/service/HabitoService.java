package com.backend.nutri_predic.alimentacion.habito.service;

import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.common.exception.ConflictException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.alimentacion.habito.dto.*;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import com.backend.nutri_predic.suplemento.repository.RegistroConsumoSuplementoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HabitoService {
    private final RegistroHabitoRepository registros;
    private final AccessService accessService;
    private final RegistroAlimentoRepository alimentos;
    private final RegistroConsumoSuplementoRepository consumos;
    private final PlanDiarioService planes;

    public HabitoService(
            RegistroHabitoRepository registros,
            AccessService accessService,
            RegistroAlimentoRepository alimentos,
            RegistroConsumoSuplementoRepository consumos,
            PlanDiarioService planes) {
        this.registros = registros;
        this.accessService = accessService;
        this.alimentos = alimentos;
        this.consumos = consumos;
        this.planes = planes;
    }

    @Transactional
    public HabitoResponse create(HabitoRequest r, Authentication auth) {
        var cliente = accessService.client(r.clienteId(), auth);
        ensureUniqueDate(cliente.getId(), r.fecha(), null);
        var h = new RegistroHabito();
        h.setCliente(cliente);
        apply(
                h,
                r.fecha(),
                r.cantidadComidas(),
                r.consumoAgua(),
                r.proteinas(),
                r.tipoAlimentacion(),
                r.nivelOrganizacion(),
                r.desayuno(),
                r.snacks(),
                r.alimentos(),
                r.comidasCocinadas(),
                r.restricciones(),
                r.consumeSuplementos());
        var guardado = registros.save(h);
        planes.recalcularSiExiste(cliente.getId(), guardado.getFecha());
        return HabitoResponse.from(guardado);
    }

    @Transactional(readOnly = true)
    public List<HabitoResponse> list(Long clienteId, Authentication auth) {
        accessService.client(clienteId, auth);
        return registros.findByClienteIdOrderByFechaDesc(clienteId).stream()
                .map(HabitoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HabitoResponse get(Long clienteId, LocalDate fecha, Authentication auth) {
        accessService.client(clienteId, auth);
        return registros
                .findByClienteIdAndFecha(clienteId, fecha)
                .map(HabitoResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito"));
    }

    @Transactional
    public HabitoResponse update(Long id, HabitoUpdateRequest r, Authentication auth) {
        var h = find(id);
        var fechaAnterior = h.getFecha();
        accessService.client(h.getCliente().getId(), auth);
        ensureUniqueDate(h.getCliente().getId(), r.fecha(), h);
        apply(
                h,
                r.fecha(),
                r.cantidadComidas(),
                r.consumoAgua(),
                r.proteinas(),
                r.tipoAlimentacion(),
                r.nivelOrganizacion(),
                r.desayuno(),
                r.snacks(),
                r.alimentos(),
                r.comidasCocinadas(),
                r.restricciones(),
                r.consumeSuplementos());
        if (consumos.existsByRegistroHabitoId(id)) h.setConsumeSuplementos(true);
        var guardado = registros.save(h);
        planes.recalcularSiExiste(h.getCliente().getId(), fechaAnterior);
        planes.recalcularSiExiste(h.getCliente().getId(), guardado.getFecha());
        return HabitoResponse.from(guardado);
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        var h = find(id);
        accessService.client(h.getCliente().getId(), auth);
        consumos.deleteByRegistroHabitoId(id);
        alimentos.deleteByRegistroHabitoId(id);
        registros.delete(h);
        registros.flush();
        planes.recalcularSiExiste(h.getCliente().getId(), h.getFecha());
    }

    private RegistroHabito find(Long id) {
        return registros.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hábito"));
    }

    private void ensureUniqueDate(Long clienteId, LocalDate fecha, RegistroHabito current) {
        if ((current == null || !fecha.equals(current.getFecha()))
                && registros.existsByClienteIdAndFecha(clienteId, fecha))
            throw new ConflictException("Ya existe un registro de hábitos para esa fecha");
    }

    private void apply(
            RegistroHabito h,
            LocalDate fecha,
            Integer comidas,
            Double agua,
            Double proteinas,
            String tipo,
            String organizacion,
            Boolean desayuno,
            Boolean snacks,
            String alimentos,
            Integer cocinadas,
            String restricciones,
            Boolean suplementos) {
        h.setFecha(fecha);
        h.setCantidadComidas(comidas);
        h.setConsumoAgua(agua);
        h.setProteinas(proteinas);
        h.setTipoAlimentacion(trimToNull(tipo));
        h.setNivelOrganizacion(trimToNull(organizacion));
        h.setDesayuno(desayuno);
        h.setSnacks(snacks);
        h.setAlimentos(trimToNull(alimentos));
        h.setComidasCocinadas(cocinadas);
        h.setRestricciones(trimToNull(restricciones));
        h.setConsumeSuplementos(suplementos);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
