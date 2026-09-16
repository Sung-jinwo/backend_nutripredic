package com.backend.nutri_predic.consumo.service;

import com.backend.nutri_predic.alimentacion.dto.AporteNutricionalCalculado;
import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.alimentacion.service.NutricionAlimentoService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.consumo.classification.PcsClassificationEngine;
import com.backend.nutri_predic.consumo.classification.PcsComponentContribution;
import com.backend.nutri_predic.consumo.classification.PcsEvaluationResult;
import com.backend.nutri_predic.consumo.classification.PcsFactualSnapshot;
import com.backend.nutri_predic.consumo.classification.PcsObservedValue;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoRequest;
import com.backend.nutri_predic.consumo.dto.EvaluacionConsumoResponse;
import com.backend.nutri_predic.consumo.entity.EstadoClasificacionConsumo;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.EvaluacionConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import com.backend.nutri_predic.consumo.entity.SnapshotEvaluacionConsumo;
import com.backend.nutri_predic.consumo.repository.CriterioConsumoSuplementosRepository;
import com.backend.nutri_predic.consumo.repository.DetalleEvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.repository.RubricaConsumoSuplementosRepository;
import com.backend.nutri_predic.consumo.repository.SnapshotEvaluacionConsumoRepository;
import com.backend.nutri_predic.suplemento.dto.AporteSuplementoCalculado;
import com.backend.nutri_predic.suplemento.entity.RegistroConsumoSuplemento;
import com.backend.nutri_predic.suplemento.repository.RegistroConsumoSuplementoRepository;
import com.backend.nutri_predic.suplemento.repository.SuplementoClienteRepository;
import com.backend.nutri_predic.suplemento.service.NutricionSuplementoService;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import tools.jackson.databind.ObjectMapper;

@Service
public class ConsumoEvaluacionExtractor {
    public static final int MAX_VENTANA_DIAS = 31;
    private final ClienteRepository clientes;
    private final RegistroConsumoSuplementoRepository consumos;
    private final SuplementoClienteRepository habituales;
    private final EvaluacionConsumoRepository evaluaciones;
    private final SnapshotEvaluacionConsumoRepository snapshots;
    private final RubricaConsumoSuplementosRepository rubricas;
    private final CriterioConsumoSuplementosRepository criterios;
    private final NutricionSuplementoService nutricion;
    private final PcsClassificationEngine clasificador;
    private final RegistroAlimentoRepository alimentos;
    private final NutricionAlimentoService nutricionAlimentos;
    private final ObjectMapper json;
    private final PrediccionModeloRepository predicciones;
    private final DetalleEvaluacionConsumoRepository detalles;

    public ConsumoEvaluacionExtractor(
            ClienteRepository clientes,
            RegistroConsumoSuplementoRepository consumos,
            SuplementoClienteRepository habituales,
            EvaluacionConsumoRepository evaluaciones,
            SnapshotEvaluacionConsumoRepository snapshots,
            RubricaConsumoSuplementosRepository rubricas,
            CriterioConsumoSuplementosRepository criterios,
            NutricionSuplementoService nutricion,
            PcsClassificationEngine clasificador,
            RegistroAlimentoRepository alimentos,
            NutricionAlimentoService nutricionAlimentos,
            ObjectMapper json,
            PrediccionModeloRepository predicciones,
            DetalleEvaluacionConsumoRepository detalles) {
        this.clientes = clientes;
        this.consumos = consumos;
        this.habituales = habituales;
        this.evaluaciones = evaluaciones;
        this.snapshots = snapshots;
        this.rubricas = rubricas;
        this.criterios = criterios;
        this.nutricion = nutricion;
        this.clasificador = clasificador;
        this.alimentos = alimentos;
        this.nutricionAlimentos = nutricionAlimentos;
        this.json = json;
        this.predicciones = predicciones;
        this.detalles = detalles;
    }

    @Transactional
    public EvaluacionConsumoResponse extraer(EvaluacionConsumoRequest request) {
        return extraerInterno(request, false);
    }

    /** Punto idempotente utilizado exclusivamente por el ciclo posterior V5. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EvaluacionConsumoResponse extraerAutomatico(EvaluacionConsumoRequest request) {
        return extraerInterno(request, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void vincularPrediccionAutomatica(Long evaluacionId, Long prediccionId) {
        var evaluacion = evaluaciones.findById(evaluacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluación de consumo"));
        if (evaluacion.getPrediccionModelo() == null) {
            evaluacion.setPrediccionModelo(predicciones.getReferenceById(prediccionId));
            evaluaciones.save(evaluacion);
        }
    }

    private EvaluacionConsumoResponse extraerInterno(EvaluacionConsumoRequest request, boolean reutilizar) {
        if (request.fechaCorte().isAfter(LocalDate.now(ZoneOffset.UTC)))
            throw new IllegalArgumentException("fechaCorte PCS no puede ser futura");
        if (request.ventanaDias() < 1 || request.ventanaDias() > MAX_VENTANA_DIAS)
            throw new IllegalArgumentException("ventanaDias PCS debe estar entre 1 y " + MAX_VENTANA_DIAS);
        var cliente =
                clientes.findById(request.clienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var inicio = request.fechaCorte().minusDays(request.ventanaDias() - 1L);
        if (reutilizar) {
            var existente = evaluaciones
                    .findFirstByClienteIdAndFechaCorteAndFechaInicioAndVentanaDiasAndSchemaVersionOrderByFechaEvaluacionDesc(
                            cliente.getId(), request.fechaCorte(), inicio, request.ventanaDias(), "pcs-factual-v1");
            if (existente.isPresent()) return EvaluacionConsumoResponse.from(existente.get());
        }
        var reales =
                consumos
                        .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                                cliente.getId(), inicio, request.fechaCorte());
        var rubrica = rubricaAplicable(request.fechaCorte(), request.ventanaDias());
        boolean poblacionNoAplicable = rubrica != null
                && "PCS_SUPLEMENTACION_DIARIA_ADULTOS".equals(rubrica.getCodigo())
                && cliente.getEdad() != null
                && cliente.getEdad() < 18;
        if (poblacionNoAplicable) rubrica = null;
        var consumosFactuales = reales.stream().map(this::factual).toList();
        var aportesAlimentos =
                alimentos
                        .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                                cliente.getId(), inicio, request.fechaCorte())
                        .stream()
                        .map(nutricionAlimentos::calcular)
                        .toList();
        var snapshotClasificacion =
                new PcsFactualSnapshot(
                        request.fechaCorte(),
                        request.ventanaDias(),
                        consumosFactuales.stream()
                                .flatMap(item -> observaciones(item).stream())
                                .toList(),
                        componentes(aportesAlimentos, consumosFactuales));
        PcsEvaluationResult resultadoTecnico =
                clasificador.evaluar(
                        snapshotClasificacion,
                        rubrica,
                        rubrica == null
                                ? List.of()
                                : criterios.findByRubricaIdOrderByIdAsc(rubrica.getId()));

        var evaluacion = new EvaluacionConsumo();
        evaluacion.setCliente(cliente);
        evaluacion.setRubrica(rubrica);
        evaluacion.setFechaCorte(request.fechaCorte());
        evaluacion.setFechaInicio(inicio);
        evaluacion.setVentanaDias(request.ventanaDias());
        evaluacion.setMomento(
                request.momento() == null ? MomentoEvaluacion.NO_DETERMINADO : request.momento());
        evaluacion.setEstadoClasificacion(resultadoTecnico.estadoClasificacion());
        evaluacion.setMotivoClasificacion(resultadoTecnico.motivo());
        evaluacion.setEstadoValidez(
                resultadoTecnico.estadoClasificacion() == EstadoClasificacionConsumo.NO_DETERMINADA
                        ? EstadoValidezMedicion.NO_DETERMINADA
                        : EstadoValidezMedicion.VALIDA);
        evaluacion.setSchemaVersion("pcs-factual-v1");
        evaluacion.setAltoConsumo(resultadoTecnico.altoConsumo());
        evaluacion.setAdvertencias(
                poblacionNoAplicable
                        ? "La referencia configurada corresponde a adultos sanos y no se aplica a menores de 18 años."
                        : reales.isEmpty() ? "Sin consumo real estructurado en la ventana." : null);
        evaluacion = evaluaciones.save(evaluacion);
        persistirDetalles(evaluacion, resultadoTecnico);

        try {
            var datos =
                    new SnapshotFactual(
                            request.fechaCorte(),
                            inicio,
                            request.ventanaDias(),
                            consumosFactuales,
                            habituales
                                    .findByClienteIdOrderBySuplementoNombreAscIdAsc(cliente.getId())
                                    .stream()
                                    .map(
                                            x ->
                                                    new FrecuenciaConfigurada(
                                                            x.getId(),
                                                            x.getSuplemento().getId(),
                                                            x.getSuplemento().getNombre(),
                                                            x.getFrecuencia(),
                                                            x.getTomasPorPeriodo(),
                                                            x.getPeriodoFrecuencia() == null
                                                                    ? null
                                                                    : x.getPeriodoFrecuencia()
                                                                            .name()))
                                    .toList(),
                            resultadoTecnico);
            var snapshot = new SnapshotEvaluacionConsumo();
            snapshot.setEvaluacion(evaluacion);
            snapshot.setSchemaVersion("pcs-factual-v1");
            snapshot.setContenidoJson(json.writeValueAsString(datos));
            snapshots.save(snapshot);
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo crear el snapshot factual", error);
        }
        return EvaluacionConsumoResponse.from(evaluacion);
    }

    private void persistirDetalles(EvaluacionConsumo evaluacion, PcsEvaluationResult resultado) {
        for (var traza : resultado.trazas()) {
            var detalle = new com.backend.nutri_predic.consumo.entity.DetalleEvaluacionConsumo();
            detalle.setEvaluacion(evaluacion);
            detalle.setCriterioEvaluadoId(traza.criterioId());
            detalle.setComponente(traza.componente());
            detalle.setFuente(fuente(traza));
            detalle.setCantidadObservada(traza.valorObservadoFinal());
            detalle.setReferenciaAplicada(traza.valorReferencia());
            detalle.setReferenciaAplicadaHasta(traza.valorReferenciaHasta());
            detalle.setUnidad(traza.unidad());
            detalle.setOperador(traza.operador() == null ? null : traza.operador().name());
            detalle.setResultado(traza.resultado().name());
            detalle.setMotivoNoCalculable(traza.motivoNoCalculable());
            detalles.save(detalle);
        }
    }

    private String fuente(com.backend.nutri_predic.consumo.classification.PcsCriterionTrace traza) {
        if (traza.ambitoAporte() == null
                || traza.ambitoAporte()
                        == com.backend.nutri_predic.consumo.classification.AmbitoAportePcs.SOLO_SUPLEMENTOS)
            return "SUPLEMENTACION";
        if (traza.aporteAlimentos() != null && traza.aporteSuplementos() == null) return "ALIMENTACION";
        if (traza.aporteAlimentos() == null && traza.aporteSuplementos() != null) return "SUPLEMENTACION";
        return "TOTAL_DIETA";
    }

    private RubricaConsumoSuplementos rubricaAplicable(
            LocalDate fechaCorte, Integer ventanaDias) {
        Instant inicioCorte = fechaCorte.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant finCorte =
                fechaCorte.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);
        return rubricas.findConCriteriosOrderByVersionDescIdDesc().stream()
                .filter(r -> r.getEstado() == EstadoCriterioConsumo.ACTIVO)
                .filter(RubricaConsumoSuplementos::isValidada)
                .filter(r -> r.getVentanaDias() == null || r.getVentanaDias().equals(ventanaDias))
                .filter(r -> r.getVigenteDesde() == null || !r.getVigenteDesde().isAfter(finCorte))
                .filter(
                        r ->
                                r.getVigenteHasta() == null
                                        || !r.getVigenteHasta().isBefore(inicioCorte))
                .findFirst()
                .orElse(null);
    }

    private ConsumoFactual factual(RegistroConsumoSuplemento registro) {
        var asignacion = registro.getSuplementoCliente();
        var composicion = registro.getComposicionSuplemento();
        AporteSuplementoCalculado aporte = nutricion.calcular(registro);
        String unidadNormalizada =
                aporte.calculable() && composicion != null && composicion.getUnidadPorcion() != null
                        ? composicion.getUnidadPorcion().getCodigo()
                        : null;
        return new ConsumoFactual(
                registro.getRegistroHabito().getFecha(),
                asignacion.getId(),
                asignacion.getSuplemento().getId(),
                asignacion.getSuplemento().getNombre(),
                registro.getCantidadConsumida(),
                registro.getUnidad().getCodigo(),
                unidadNormalizada,
                registro.getNumeroTomas(),
                asignacion.getFrecuencia(),
                asignacion.getTomasPorPeriodo(),
                asignacion.getPeriodoFrecuencia() == null
                        ? null
                        : asignacion.getPeriodoFrecuencia().name(),
                composicion == null ? null : composicion.getId(),
                composicion != null,
                registro.getEquivalenciaUnidad() == null
                        ? null
                        : registro.getEquivalenciaUnidad().getId(),
                aporte);
    }

    private List<PcsObservedValue> observaciones(ConsumoFactual factual) {
        List<PcsObservedValue> valores = new ArrayList<>();
        valores.add(
                new PcsObservedValue(
                        factual.suplementoId(),
                        null,
                        factual.suplemento(),
                        "CANTIDAD_CONSUMIDA",
                        factual.cantidadConsumida(),
                        factual.unidadRegistrada(),
                        false));
        boolean normalizado = factual.componentesNormalizados().calculable();
        agregarComponente(
                valores,
                factual,
                "PROTEINA",
                factual.componentesNormalizados().proteinaG(),
                "G",
                normalizado);
        agregarComponente(
                valores,
                factual,
                "CREATINA",
                factual.componentesNormalizados().creatinaG(),
                "G",
                normalizado);
        agregarComponente(
                valores,
                factual,
                "CAFEINA",
                factual.componentesNormalizados().cafeinaMg(),
                "MG",
                normalizado);
        agregarComponente(
                valores,
                factual,
                "CARBOHIDRATOS",
                factual.componentesNormalizados().carbohidratosG(),
                "G",
                normalizado);
        agregarComponente(
                valores,
                factual,
                "GRASAS",
                factual.componentesNormalizados().grasasG(),
                "G",
                normalizado);
        agregarComponente(
                valores,
                factual,
                "SODIO",
                factual.componentesNormalizados().sodioMg(),
                "MG",
                normalizado);
        return valores;
    }

    private void agregarComponente(
            List<PcsObservedValue> valores,
            ConsumoFactual factual,
            String componente,
            BigDecimal cantidad,
            String unidad,
            boolean normalizado) {
        valores.add(
                new PcsObservedValue(
                        factual.suplementoId(),
                        componente,
                        componente,
                        "APORTE_COMPONENTE",
                        cantidad,
                        unidad,
                        normalizado));
    }

    private List<PcsComponentContribution> componentes(
            List<AporteNutricionalCalculado> alimentos, List<ConsumoFactual> suplementos) {
        return List.of(
                componente(
                        "PROTEINA",
                        "G",
                        alimentos,
                        AporteNutricionalCalculado::proteinaG,
                        suplementos,
                        a -> a.componentesNormalizados().proteinaG()),
                componente(
                        "CREATINA",
                        "G",
                        List.of(),
                        a -> null,
                        suplementos,
                        a -> a.componentesNormalizados().creatinaG()),
                componente(
                        "CAFEINA",
                        "MG",
                        List.of(),
                        a -> null,
                        suplementos,
                        a -> a.componentesNormalizados().cafeinaMg()),
                componente(
                        "CARBOHIDRATOS",
                        "G",
                        alimentos,
                        AporteNutricionalCalculado::carbohidratosG,
                        suplementos,
                        a -> a.componentesNormalizados().carbohidratosG()),
                componente(
                        "GRASAS",
                        "G",
                        alimentos,
                        AporteNutricionalCalculado::grasasG,
                        suplementos,
                        a -> a.componentesNormalizados().grasasG()),
                componente(
                        "SODIO",
                        "MG",
                        alimentos,
                        AporteNutricionalCalculado::sodioMg,
                        suplementos,
                        a -> a.componentesNormalizados().sodioMg()));
    }

    private PcsComponentContribution componente(
            String nombre,
            String unidad,
            List<AporteNutricionalCalculado> alimentos,
            Function<AporteNutricionalCalculado, BigDecimal> valorAlimento,
            List<ConsumoFactual> suplementos,
            Function<ConsumoFactual, BigDecimal> valorSuplemento) {
        SumaCalculable comida =
                sumar(alimentos, AporteNutricionalCalculado::calculable, valorAlimento);
        SumaCalculable suplemento =
                sumar(
                        suplementos,
                        item -> item.componentesNormalizados().calculable(),
                        valorSuplemento);
        return new PcsComponentContribution(
                nombre,
                comida.valor(),
                comida.calculable(),
                suplemento.valor(),
                suplemento.calculable(),
                unidad);
    }

    private <T> SumaCalculable sumar(
            List<T> registros,
            java.util.function.Predicate<T> baseCalculable,
            Function<T, BigDecimal> valor) {
        if (registros.isEmpty()) return new SumaCalculable(null, false);
        BigDecimal suma = BigDecimal.ZERO;
        for (T registro : registros) {
            BigDecimal actual = valor.apply(registro);
            if (!baseCalculable.test(registro) || actual == null) {
                return new SumaCalculable(null, false);
            }
            suma = suma.add(actual);
        }
        return new SumaCalculable(suma, true);
    }

    private record SnapshotFactual(
            LocalDate fechaCorte,
            LocalDate fechaInicio,
            Integer ventanaDias,
            List<ConsumoFactual> consumosReales,
            List<FrecuenciaConfigurada> habitualesContexto,
            PcsEvaluationResult evaluacionTecnica) {}

    private record ConsumoFactual(
            LocalDate fecha,
            Long suplementoClienteId,
            Long suplementoId,
            String suplemento,
            BigDecimal cantidadConsumida,
            String unidadRegistrada,
            String unidadNormalizada,
            Integer numeroTomas,
            String frecuenciaConfigurada,
            Integer tomasPorPeriodo,
            String periodoFrecuencia,
            Long composicionId,
            boolean composicionDisponible,
            Long equivalenciaId,
            AporteSuplementoCalculado componentesNormalizados) {}

    private record FrecuenciaConfigurada(
            Long suplementoClienteId,
            Long suplementoId,
            String suplemento,
            String frecuencia,
            Integer tomasPorPeriodo,
            String periodoFrecuencia) {}

    private record SumaCalculable(BigDecimal valor, boolean calculable) {}
}
