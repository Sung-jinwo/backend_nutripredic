package com.backend.nutri_predic.perfilhabitos.service;

import com.backend.nutri_predic.alimentacion.dto.ResumenNutricionalDiario;
import com.backend.nutri_predic.alimentacion.service.AgregacionNutricionalService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.perfilhabitos.dto.EvidenciaPerfilHabitosV6Response;
import com.backend.nutri_predic.perfilhabitos.entity.*;
import com.backend.nutri_predic.perfilhabitos.repository.*;
import com.backend.nutri_predic.requerimientonutricional.service.RequerimientoNutricionalService;
import com.backend.nutri_predic.suplemento.dto.ResumenSuplementacionDiario;
import com.backend.nutri_predic.suplemento.service.AgregacionSuplementacionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenciaPerfilHabitosV6Service {
    private static final String CODIGO_RUBRICA = "RUBRICA_PERFIL_HABITOS_V1";
    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal COBERTURA_MINIMA = new BigDecimal("70.00");

    private final ClienteRepository clientes;
    private final RubricaPerfilHabitosRepository rubricas;
    private final DimensionRubricaPerfilHabitosRepository dimensiones;
    private final CriterioRubricaPerfilHabitosRepository criterios;
    private final RegistroHabitoRepository habitos;
    private final AgregacionNutricionalService alimentacion;
    private final AgregacionSuplementacionService suplementacion;
    private final RequerimientoNutricionalService requerimientos;
    private final ObjectMapper json = new ObjectMapper();

    public EvidenciaPerfilHabitosV6Service(
            ClienteRepository clientes,
            RubricaPerfilHabitosRepository rubricas,
            DimensionRubricaPerfilHabitosRepository dimensiones,
            CriterioRubricaPerfilHabitosRepository criterios,
            RegistroHabitoRepository habitos,
            AgregacionNutricionalService alimentacion,
            AgregacionSuplementacionService suplementacion,
            RequerimientoNutricionalService requerimientos) {
        this.clientes = clientes;
        this.rubricas = rubricas;
        this.dimensiones = dimensiones;
        this.criterios = criterios;
        this.habitos = habitos;
        this.alimentacion = alimentacion;
        this.suplementacion = suplementacion;
        this.requerimientos = requerimientos;
    }

    @Transactional(readOnly = true)
    public EvidenciaPerfilHabitosV6Response calcular(Long clienteId, LocalDate fechaCorte) {
        var cliente =
                clientes.findById(clienteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var rubrica =
                rubricas.findAllByOrderByCodigoAscVersionDesc().stream()
                        .filter(r -> CODIGO_RUBRICA.equals(r.getCodigo()))
                        .filter(r -> r.getEstado() == EstadoRubricaPerfilHabitos.ACTIVA)
                        .filter(r -> r.getValidadoEn() != null)
                        .filter(r -> r.getVigenteDesde() != null)
                        .filter(r -> !r.getVigenteDesde().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(fechaCorte))
                        .filter(r -> r.getVigenteHasta() == null || !r.getVigenteHasta().atZone(ZoneId.systemDefault()).toLocalDate().isBefore(fechaCorte))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Rúbrica ACTIVA, VALIDADA y VIGENTE no encontrada para perfil V6"));
        var alimento = alimentacion.resumir(clienteId, fechaCorte);
        var suplemento = suplementacion.resumir(clienteId, fechaCorte);
        var desde = fechaCorte.minusDays(6);
        var habitosPorFecha = new HashMap<LocalDate, RegistroHabito>();
        for (var h :
                habitos.findByClienteIdAndFechaBetweenOrderByFechaAscIdAsc(
                        clienteId, desde, fechaCorte)) {
            habitosPorFecha.putIfAbsent(h.getFecha(), h);
        }
        var requerimiento = requerimientos.resolver(cliente, fechaCorte);
        var criteriosCalculados =
                criterios.findByRubricaIdOrderByDimensionOrdenAscOrdenAsc(rubrica.getId()).stream()
                        .map(c -> calcularCriterio(c, alimento, suplemento.resumenDiario(), habitosPorFecha, requerimiento))
                        .toList();
        var porDimension = new ArrayList<EvidenciaPerfilHabitosV6Response.EvidenciaDimension>();
        BigDecimal totalObtenido = BigDecimal.ZERO;
        BigDecimal totalMaxCalculable = BigDecimal.ZERO;
        for (var d : dimensiones.findByRubricaIdOrderByOrdenAsc(rubrica.getId())) {
            var deDimension =
                    criteriosCalculados.stream()
                            .filter(c -> c.dimensionId().equals(d.getId()))
                            .toList();
            BigDecimal obtenido = suma(deDimension, EvidenciaPerfilHabitosV6Response.EvidenciaCriterio::puntosObtenidos);
            BigDecimal maxCalculable =
                    sumaCalculable(deDimension);
            totalObtenido = totalObtenido.add(obtenido);
            totalMaxCalculable = totalMaxCalculable.add(maxCalculable);
            porDimension.add(
                    new EvidenciaPerfilHabitosV6Response.EvidenciaDimension(
                            d.getId(),
                            d.getCodigo().name(),
                            d.getNombre(),
                            d.getPesoPorcentual(),
                            escala(obtenido),
                            escala(maxCalculable),
                            porcentaje(maxCalculable, d.getPesoPorcentual())));
        }
        BigDecimal cobertura = porcentaje(totalMaxCalculable, CIEN);
        return new EvidenciaPerfilHabitosV6Response(
                clienteId,
                fechaCorte,
                rubrica.getId(),
                rubrica.getCodigo(),
                rubrica.getVersion(),
                rubrica.getEstado().name(),
                rubrica.getValidadoEn() != null,
                escala(totalObtenido),
                escala(totalMaxCalculable),
                cobertura,
                cobertura != null && cobertura.compareTo(COBERTURA_MINIMA) < 0
                        ? EstadoValidezMedicion.INVALIDA.name()
                        : EstadoValidezMedicion.NO_DETERMINADA.name(),
                List.copyOf(porDimension),
                criteriosCalculados);
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio calcularCriterio(
            CriterioRubricaPerfilHabitos c,
            com.backend.nutri_predic.alimentacion.dto.ResumenAgregacionNutricionalResponse alimentacion,
            List<ResumenSuplementacionDiario> suplementacion,
            Map<LocalDate, RegistroHabito> habitos,
            com.backend.nutri_predic.requerimientonutricional.dto.RequerimientoNutricionalResponse req) {
        if (c.getTipoEvaluacion() == TipoEvaluacionCriterioPerfil.MANUAL_ESPECIALISTA) {
            return salida(c, EstadoResultadoCriterioPerfil.MANUAL_PENDIENTE, null, null, c.getPuntosMaximos(), null,
                    "Pendiente de evaluación manual del especialista");
        }
        if (c.getTipoEvaluacion() == TipoEvaluacionCriterioPerfil.DESCRIPTIVO) {
            return salida(c, EstadoResultadoCriterioPerfil.DESCRIPTIVO, valorDescriptivo(c, suplementacion), null, null, null,
                    "Dato descriptivo; no puntúa automáticamente");
        }
        return switch (c.getCodigo()) {
            case "FIBRA_25G_DIA" -> evaluarDias(c, alimentacion.dias(), d -> d.fibraG().valorConocido(), v -> v.compareTo(param(c, "minG", "25")) >= 0);
            case "SODIO_MENOR_2000MG_DIA" -> evaluarDias(c, alimentacion.dias(), d -> d.sodioMg().valorConocido(), v -> v.compareTo(param(c, "maxMg", "2000")) < 0);
            case "CARBOHIDRATOS_45_75_ENERGIA" -> evaluarPorcentajeEnergia(c, alimentacion.dias(), d -> d.carbohidratosG().valorConocido(), new BigDecimal("4"));
            case "GRASAS_15_30_ENERGIA", "GRASAS_30_ENERGIA" -> evaluarPorcentajeEnergia(c, alimentacion.dias(), d -> d.grasasG().valorConocido(), new BigDecimal("9"));
            case "PROTEINA_083_GKG_DIA" -> evaluarProteinaKg(c, alimentacion);
            case "ENERGIA_REQUERIMIENTO_REFERENCIADO" -> energia(c, req);
            case "PREPARACIONES_CASERAS" -> evaluarHabitos(c, habitos);
            case "AGUA_1000ML_DIA", "AGUA_6_8_VASOS_DIA" -> evaluarAgua(c, habitos);
            case "CAFEINA_SUPLEMENTARIA_400MG_DIA" -> evaluarCafeinaSuplementaria(c, suplementacion);
            case "AZUCAR_LIBRE_NO_CALCULABLE" -> salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "No existe diferenciación explícita de azúcar libre o añadida; no se compara azúcar total contra OMS");
            case "FRUTAS_VERDURAS_400G_DIA" -> salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "El catálogo actual no identifica frutas y verduras con clasificación fiable para este criterio");
            case "NATURALES_PROCESADOS" -> salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "El catálogo actual no contiene clasificación fiable natural/procesado");
            case "PROTEINA_SUPLEMENTARIA_CONTEXTO" -> salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "Pendiente política validada para evaluar proteína suplementaria en contexto de proteína total");
            case "CARBOHIDRATOS_SUPLEMENTARIOS_TOTAL_DIETA", "GRASAS_SUPLEMENTARIAS_TOTAL_DIETA" -> salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "Pendiente referencia aplicable y política validada para evaluar aporte suplementario contra dieta total");
            default -> salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "El backend actual no tiene una fuente confiable para este criterio");
        };
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evaluarProteinaKg(
            CriterioRubricaPerfilHabitos c,
            com.backend.nutri_predic.alimentacion.dto.ResumenAgregacionNutricionalResponse alimentacion) {
        BigDecimal peso = alimentacion.ventana().pesoAplicableKg();
        if (peso == null || peso.signum() <= 0) {
            return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "No existe peso aplicable para calcular g/kg/día");
        }
        BigDecimal min = param(c, "minGKgDia", "0.83");
        return evaluarDias(
                c,
                alimentacion.dias(),
                d -> {
                    BigDecimal proteina = d.proteinaG().valorConocido();
                    return proteina == null
                            ? null
                            : proteina.divide(peso, 6, RoundingMode.HALF_UP);
                },
                v -> v.compareTo(min) >= 0);
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio energia(
            CriterioRubricaPerfilHabitos c,
            com.backend.nutri_predic.requerimientonutricional.dto.RequerimientoNutricionalResponse req) {
        if (!"DISPONIBLE".equals(req.estado()) || req.kcalObjetivo() == null) {
            return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    req.motivoNoDisponible() == null ? "Sin requerimiento energético válido" : req.motivoNoDisponible());
        }
        return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, req.kcalObjetivo(), null, null, null,
                "Existe requerimiento, pero falta política validada de rango de adecuación energética");
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evaluarPorcentajeEnergia(
            CriterioRubricaPerfilHabitos c,
            List<ResumenNutricionalDiario> dias,
            Function<ResumenNutricionalDiario, BigDecimal> gramos,
            BigDecimal kcalPorGramo) {
        BigDecimal min = param(c, "porcentajeMin", "0");
        BigDecimal max = param(c, "porcentajeMax", "100");
        return evaluarDias(
                c,
                dias,
                d -> {
                    BigDecimal kcal = d.kcal().valorConocido();
                    BigDecimal g = gramos.apply(d);
                    return kcal == null || kcal.signum() <= 0 || g == null
                            ? null
                            : g.multiply(kcalPorGramo)
                                    .multiply(CIEN)
                                    .divide(kcal, 6, RoundingMode.HALF_UP);
                },
                v -> v.compareTo(min) >= 0 && v.compareTo(max) <= 0);
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evaluarHabitos(
            CriterioRubricaPerfilHabitos c, Map<LocalDate, RegistroHabito> habitos) {
        BigDecimal min = param(c, "minComidasCocinadasDia", "1");
        int cumple = 0, calculables = 0;
        BigDecimal ultimo = null;
        for (var h : habitos.values()) {
            if (h.getComidasCocinadas() == null) continue;
            calculables++;
            ultimo = BigDecimal.valueOf(h.getComidasCocinadas());
            if (ultimo.compareTo(min) >= 0) cumple++;
        }
        return desdeDias(c, cumple, calculables, ultimo);
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evaluarAgua(
            CriterioRubricaPerfilHabitos c, Map<LocalDate, RegistroHabito> habitos) {
        Map<String, Object> parametros = map(c.getParametrosJson());
        Object litrosMinimosConfigurados = parametros.get("litrosMinimos");
        if (litrosMinimosConfigurados != null) {
            BigDecimal litrosMinimos = new BigDecimal(litrosMinimosConfigurados.toString());
            if (litrosMinimos.signum() <= 0) {
                return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                        "El mínimo de litros configurado no es válido");
            }
            int cumple = 0, calculables = 0;
            BigDecimal ultimo = null;
            for (var h : habitos.values()) {
                if (h.getConsumoAgua() == null) continue;
                calculables++;
                ultimo = BigDecimal.valueOf(h.getConsumoAgua());
                if (ultimo.compareTo(litrosMinimos) >= 0) cumple++;
            }
            return desdeDias(c, cumple, calculables, ultimo);
        }
        Object equivalencia = parametros.get("equivalenciaMlPorVaso");
        if (equivalencia == null) {
            return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "La referencia es 6–8 vasos/día; no existe una equivalencia ml/vaso documentada y adoptada por el sistema");
        }
        BigDecimal mlPorVaso = new BigDecimal(equivalencia.toString());
        if (mlPorVaso.signum() <= 0) {
            return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, null, null, null, null,
                    "La equivalencia ml/vaso configurada no es válida");
        }
        BigDecimal vasosMinimos = param(c, "vasosMinimos", "6");
        int cumple = 0, calculables = 0;
        BigDecimal ultimo = null;
        for (var h : habitos.values()) {
            if (h.getConsumoAgua() == null) continue;
            calculables++;
            BigDecimal ml = BigDecimal.valueOf(h.getConsumoAgua()).multiply(new BigDecimal("1000"));
            ultimo = ml.divide(mlPorVaso, 6, RoundingMode.HALF_UP);
            if (ultimo.compareTo(vasosMinimos) >= 0) cumple++;
        }
        return desdeDias(c, cumple, calculables, ultimo);
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evaluarCafeinaSuplementaria(
            CriterioRubricaPerfilHabitos c, List<ResumenSuplementacionDiario> dias) {
        BigDecimal maxMg = param(c, "maxMg", "400");
        int sinExceso = 0, calculables = 0;
        BigDecimal maxObservado = null;
        for (var dia : dias) {
            BigDecimal cafeina = dia.cafeinaMg();
            if (cafeina == null) continue;
            calculables++;
            if (maxObservado == null || cafeina.compareTo(maxObservado) > 0) maxObservado = cafeina;
            if (cafeina.compareTo(maxMg) <= 0) sinExceso++;
        }
        if (calculables < 7) {
            return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, maxObservado, null, null, sinExceso,
                    "No hay 7 días calculables de cafeína proveniente de suplementos");
        }
        boolean supera = maxObservado != null && maxObservado.compareTo(maxMg) > 0;
        return salida(
                c,
                supera
                        ? EstadoResultadoCriterioPerfil.SUPERA_UMBRAL_TOTAL_DESDE_SUPLEMENTOS
                        : EstadoResultadoCriterioPerfil.SIN_EXCESO_DOCUMENTADO_SOLO_POR_SUPLEMENTOS,
                maxObservado,
                puntosPorDias(c, sinExceso),
                c.getPuntosMaximos(),
                sinExceso,
                supera
                        ? "Supera 400 mg/día desde suplementos; EFSA aplica el umbral al total de fuentes."
                        : "No documenta exceso desde suplementos; no permite afirmar seguridad del consumo total de cafeína.");
    }

    private <T> EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evaluarDias(
            CriterioRubricaPerfilHabitos c,
            List<T> dias,
            Function<T, BigDecimal> valor,
            java.util.function.Predicate<BigDecimal> cumple) {
        int ok = 0, calculables = 0;
        BigDecimal ultimo = null;
        for (var dia : dias) {
            BigDecimal v = valor.apply(dia);
            if (v == null) continue;
            calculables++;
            ultimo = v;
            if (cumple.test(v)) ok++;
        }
        return desdeDias(c, ok, calculables, ultimo);
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio desdeDias(
            CriterioRubricaPerfilHabitos c, int cumple, int calculables, BigDecimal valor) {
        if (calculables < 7) {
            return salida(c, EstadoResultadoCriterioPerfil.NO_CALCULABLE, valor, null, null, cumple,
                    "No hay 7 días calculables para aplicar adherencia semanal");
        }
        BigDecimal puntos = puntosPorDias(c, cumple);
        return salida(c, cumple > 0 ? EstadoResultadoCriterioPerfil.CUMPLE : EstadoResultadoCriterioPerfil.NO_CUMPLE,
                valor, puntos, c.getPuntosMaximos(), cumple, null);
    }

    private BigDecimal valorDescriptivo(CriterioRubricaPerfilHabitos c, List<ResumenSuplementacionDiario> dias) {
        if ("CREATINA_DESCRIPTIVA".equals(c.getCodigo())) {
            return dias.stream().map(ResumenSuplementacionDiario::creatinaG).filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(7), 6, RoundingMode.HALF_UP);
        }
        return null;
    }

    private EvidenciaPerfilHabitosV6Response.EvidenciaCriterio salida(
            CriterioRubricaPerfilHabitos c,
            EstadoResultadoCriterioPerfil estado,
            BigDecimal valor,
            BigDecimal puntos,
            BigDecimal max,
            Integer diasCumple,
            String motivo) {
        return new EvidenciaPerfilHabitosV6Response.EvidenciaCriterio(
                c.getId(),
                c.getDimension().getId(),
                c.getCodigo(),
                c.getNombre(),
                c.getTipoEvaluacion().name(),
                estado.name(),
                escala(valor),
                escala(puntos),
                escala(max),
                diasCumple,
                c.getReferencia(),
                motivo,
                c.getFuente(),
                c.getTipoFuente().name());
    }

    private BigDecimal puntosPorDias(CriterioRubricaPerfilHabitos c, int dias) {
        var tabla = map(c.getAdherencia7dJson());
        String clave = Integer.toString(dias);
        Object valor = tabla.getOrDefault(clave, BigDecimal.ZERO);
        return new BigDecimal(valor.toString());
    }

    private BigDecimal param(CriterioRubricaPerfilHabitos c, String key, String defecto) {
        Object valor = map(c.getParametrosJson()).getOrDefault(key, defecto);
        return new BigDecimal(valor.toString());
    }

    private Map<String, Object> map(String raw) {
        if (raw == null || raw.isBlank()) return Map.of();
        try {
            return json.readValue(raw, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private BigDecimal suma(
            List<EvidenciaPerfilHabitosV6Response.EvidenciaCriterio> criterios,
            Function<EvidenciaPerfilHabitosV6Response.EvidenciaCriterio, BigDecimal> getter) {
        return criterios.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumaCalculable(List<EvidenciaPerfilHabitosV6Response.EvidenciaCriterio> criterios) {
        return criterios.stream()
                .filter(c -> c.puntosObtenidos() != null)
                .map(EvidenciaPerfilHabitosV6Response.EvidenciaCriterio::puntosMaximos)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        return parte == null || total == null || total.signum() <= 0
                ? null
                : parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }
}
