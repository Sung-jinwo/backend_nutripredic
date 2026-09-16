package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.*;
import com.backend.nutri_predic.alimentacion.repository.*;
import com.backend.nutri_predic.common.exception.*;
import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlimentacionService {
    private final AlimentoCatalogoRepository catalogo;
    private final RegistroAlimentoRepository registros;
    private final RegistroHabitoRepository habitos;
    private final UnidadMedidaRepository unidades;
    private final AccessService access;
    private final ComposicionNutricionalAlimentoService composiciones;
    private final EquivalenciaUnidadAlimentoService equivalencias;
    private final PlanDiarioService planes;

    public AlimentacionService(
            AlimentoCatalogoRepository c,
            RegistroAlimentoRepository r,
            RegistroHabitoRepository h,
            UnidadMedidaRepository u,
            AccessService a,
            ComposicionNutricionalAlimentoService cn,
            EquivalenciaUnidadAlimentoService e,
            PlanDiarioService planes) {
        catalogo = c;
        registros = r;
        habitos = h;
        unidades = u;
        access = a;
        composiciones = cn;
        equivalencias = e;
        this.planes = planes;
    }

    @Transactional(readOnly = true)
    public List<AlimentoCatalogoResponse> catalogo() {
        java.time.LocalDate hoy = java.time.LocalDate.now();
        return catalogo.findByActivoTrueOrderByNombreAsc().stream()
                .map(a -> {
                    var comp = composiciones.resolverActiva(a.getId(), hoy).orElse(null);
                    return AlimentoCatalogoResponse.fromConComposicion(a, comp);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public AlimentoCatalogoResponse detalle(Long id) {
        var a = catalogo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        if (!a.isActivo()) throw new ResourceNotFoundException("Alimento");
        var comp = composiciones.resolverActiva(a.getId(), java.time.LocalDate.now()).orElse(null);
        return AlimentoCatalogoResponse.fromConComposicion(a, comp);
    }

    @Transactional
    public AlimentoCatalogoResponse guardarCatalogo(Long id, AlimentoCatalogoRequest r) {
        if (id == null && catalogo.existsByNombreIgnoreCase(r.nombre().trim()))
            throw new ConflictException("El alimento ya existe en el catálogo");
        var a =
                id == null
                        ? new AlimentoCatalogo()
                        : catalogo.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        a.setNombre(r.nombre().trim());
        a.setCategoria(r.categoria().trim());
        a.setUnidadBase(r.unidadBaseCodigo() == null ? null : unidad(r.unidadBaseCodigo()));
        if (r.activo() != null) a.setActivo(r.activo());
        return AlimentoCatalogoResponse.from(catalogo.save(a));
    }

    @Transactional
    public void desactivarCatalogo(Long id) {
        var a = catalogo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        a.setActivo(false);
        catalogo.save(a);
    }

    @Transactional(readOnly = true)
    public List<RegistroAlimentoResponse> listarRegistro(Long habitoId, Authentication auth) {
        var h = habito(habitoId, auth);
        return registros.findByRegistroHabitoIdOrderByIdAsc(h.getId()).stream()
                .map(RegistroAlimentoResponse::from)
                .toList();
    }

    @Transactional
    public RegistroAlimentoResponse agregarRegistro(
            Long habitoId, RegistroAlimentoRequest r, Authentication auth) {
        var h = habito(habitoId, auth);
        var a = r.alimentoId() == null ? null : catalogo.findById(r.alimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        var x = new RegistroAlimento();
        x.setRegistroHabito(h);
        x.setAlimento(a);
        x.setNombreRegistrado(nombreRegistro(r, a));
        var c = a == null ? null : composiciones.resolverActiva(a.getId(), h.getFecha()).orElse(null);
        x.setComposicionNutricional(c);
        var u = unidad(r.unidadCodigo());
        x.setUnidad(u);
        if (a != null && c != null && !u.getCodigo().equalsIgnoreCase(c.getUnidadReferencia().getCodigo()))
            x.setEquivalenciaUnidad(
                    equivalencias
                            .resolver(
                                    a.getId(),
                                    u.getCodigo(),
                                    c.getUnidadReferencia().getCodigo(),
                                    h.getFecha())
                            .orElse(null));
        x.setCantidad(r.cantidad());
        x.setKcalRegistrada(calcularKcal(r)); x.setProteinaGRegistrada(r.proteinaG()); x.setCarbohidratosGRegistrados(r.carbohidratosG()); x.setGrasasGRegistradas(r.grasasG());
        x.setMomentoComida(r.momentoComida());
        var guardado = registros.save(x);
        planes.recalcularSiExiste(h.getCliente().getId(), h.getFecha());
        return RegistroAlimentoResponse.from(guardado);
    }

    @Transactional
    public RegistroAlimentoResponse actualizarRegistro(
            Long habitoId, Long id, RegistroAlimentoRequest r, Authentication auth) {
        var h = habito(habitoId, auth);
        var x =
                registros
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Registro de alimento"));
        if (!x.getRegistroHabito().getId().equals(habitoId))
            throw new ResourceNotFoundException("Registro de alimento");
        var alimento = r.alimentoId() == null ? null : catalogo.findById(r.alimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Alimento"));
        var unidad = unidad(r.unidadCodigo());
        var composicion = alimento == null ? null : composiciones.resolverActiva(alimento.getId(), h.getFecha()).orElse(null);
        x.setAlimento(alimento);
        x.setNombreRegistrado(nombreRegistro(r, alimento));
        x.setComposicionNutricional(composicion);
        x.setUnidad(unidad);
        x.setEquivalenciaUnidad(null);
        if (alimento != null && composicion != null
                && !unidad.getCodigo()
                        .equalsIgnoreCase(composicion.getUnidadReferencia().getCodigo()))
            x.setEquivalenciaUnidad(
                    equivalencias
                            .resolver(
                                    alimento.getId(),
                                    unidad.getCodigo(),
                                    composicion.getUnidadReferencia().getCodigo(),
                                    h.getFecha())
                            .orElse(null));
        x.setCantidad(r.cantidad());
        x.setKcalRegistrada(calcularKcal(r)); x.setProteinaGRegistrada(r.proteinaG()); x.setCarbohidratosGRegistrados(r.carbohidratosG()); x.setGrasasGRegistradas(r.grasasG());
        x.setMomentoComida(r.momentoComida());
        var guardado = registros.save(x);
        planes.recalcularSiExiste(h.getCliente().getId(), h.getFecha());
        return RegistroAlimentoResponse.from(guardado);
    }

    @Transactional
    public void eliminarRegistro(Long habitoId, Long id, Authentication auth) {
        habito(habitoId, auth);
        var x =
                registros
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Registro de alimento"));
        if (!x.getRegistroHabito().getId().equals(habitoId))
            throw new ResourceNotFoundException("Registro de alimento");
        registros.delete(x);
        registros.flush();
        planes.recalcularSiExiste(x.getRegistroHabito().getCliente().getId(), x.getRegistroHabito().getFecha());
    }

    @Transactional(readOnly = true)
    public List<AlimentoUsoResponse> recientes(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        return usos(clienteId, false);
    }

    @Transactional(readOnly = true)
    public List<AlimentoUsoResponse> frecuentes(Long clienteId, Authentication auth) {
        access.client(clienteId, auth);
        return usos(clienteId, true);
    }

    private com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito habito(
            Long id, Authentication auth) {
        var h = habitos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hábito"));
        access.client(h.getCliente().getId(), auth);
        return h;
    }

    private UnidadMedida unidad(String c) {
        return unidades.findByCodigoIgnoreCaseAndActivaTrue(c.trim())
                .orElseThrow(() -> new BusinessException("Unidad no válida: " + c));
    }

    private List<AlimentoUsoResponse> usos(Long clienteId, boolean ordenarFrecuencia) {
        var todos =
                registros.findByRegistroHabitoClienteIdOrderByRegistroHabitoFechaDescIdDesc(
                        clienteId);
        record Ac(RegistroAlimento ultimo, long n) {}
        Map<String, Ac> mapa = new LinkedHashMap<>();
        for (var x : todos)
            mapa.compute(
                    x.getAlimento() == null ? "nombre:" + x.getNombreRegistrado().toLowerCase(Locale.ROOT) : "catalogo:" + x.getAlimento().getId(),
                    (k, v) -> new Ac(v == null ? x : v.ultimo(), v == null ? 1 : v.n() + 1));
        var lista =
                mapa.values().stream()
                        .map(
                                a -> {
                                    var x = a.ultimo();
                                    return new AlimentoUsoResponse(
                                            x.getAlimento() == null ? null : x.getAlimento().getId(),
                                            x.getNombreRegistrado(),
                                            x.getAlimento() == null ? "PERSONALIZADO" : x.getAlimento().getCategoria(),
                                            x.getCantidad(),
                                            x.getUnidad().getCodigo(),
                                            x.getMomentoComida().name(),
                                            x.getRegistroHabito().getFecha(),
                                            a.n());
                                })
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (ordenarFrecuencia)
            lista.sort(
                    Comparator.comparingLong(AlimentoUsoResponse::vecesUtilizado)
                            .reversed()
                            .thenComparing(AlimentoUsoResponse::nombre));
        return lista.stream().limit(20).toList();
    }

    // Compatibilidad exclusivamente interna para registros de catálogo anteriores
    // a los macros declarados. El DTO HTTP mantiene @NotBlank y @NotNull.
    private String nombreRegistro(RegistroAlimentoRequest r, com.backend.nutri_predic.alimentacion.entity.AlimentoCatalogo alimento) {
        if (r.nombreAlimento() != null && !r.nombreAlimento().isBlank()) return r.nombreAlimento().trim();
        if (alimento != null && r.proteinaG() == null && r.carbohidratosG() == null && r.grasasG() == null) return alimento.getNombre();
        throw new BusinessException("El nombre del alimento es requerido");
    }

    private BigDecimal calcularKcal(RegistroAlimentoRequest r) {
        if (r.alimentoId() != null && r.proteinaG() == null && r.carbohidratosG() == null && r.grasasG() == null) return null;
        if (r.proteinaG() == null || r.carbohidratosG() == null || r.grasasG() == null) throw new BusinessException("Proteínas, carbohidratos y grasas son requeridos");
        return r.proteinaG().multiply(BigDecimal.valueOf(4))
                .add(r.carbohidratosG().multiply(BigDecimal.valueOf(4)))
                .add(r.grasasG().multiply(BigDecimal.valueOf(9)));
    }
}
