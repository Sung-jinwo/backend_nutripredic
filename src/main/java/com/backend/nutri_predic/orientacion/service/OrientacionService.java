package com.backend.nutri_predic.orientacion.service;

import com.backend.nutri_predic.alimentacion.nutricion.service.ResumenNutricionalDiarioService;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.consumo.entity.DetalleEvaluacionConsumo;
import com.backend.nutri_predic.consumo.repository.DetalleEvaluacionConsumoRepository;
import com.backend.nutri_predic.consumo.repository.EvaluacionConsumoRepository;
import com.backend.nutri_predic.orientacion.dto.OrientacionResponse;
import com.backend.nutri_predic.orientacion.entity.AdaptacionDiaria;
import com.backend.nutri_predic.orientacion.repository.AdaptacionDiariaRepository;
import com.backend.nutri_predic.plandia.entity.PlanDiario;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrientacionService {
    private static final BigDecimal LIMITE_BAJO = BigDecimal.valueOf(80);
    private static final BigDecimal LIMITE_ALTO = BigDecimal.valueOf(120);

    private final ClienteRepository clientes;
    private final ResumenNutricionalDiarioService resumenes;
    private final PrediccionModeloRepository predicciones;
    private final AdaptacionDiariaRepository adaptaciones;
    private final EvaluacionConsumoRepository evaluacionesConsumo;
    private final DetalleEvaluacionConsumoRepository detallesConsumo;
    private final ObjectMapper json;

    public OrientacionService(
            ClienteRepository clientes,
            ResumenNutricionalDiarioService resumenes,
            PrediccionModeloRepository predicciones,
            AdaptacionDiariaRepository adaptaciones,
            EvaluacionConsumoRepository evaluacionesConsumo,
            DetalleEvaluacionConsumoRepository detallesConsumo,
            ObjectMapper json) {
        this.clientes = clientes;
        this.resumenes = resumenes;
        this.predicciones = predicciones;
        this.adaptaciones = adaptaciones;
        this.evaluacionesConsumo = evaluacionesConsumo;
        this.detallesConsumo = detallesConsumo;
        this.json = json;
    }

    @Transactional(readOnly = true)
    public OrientacionResponse orientacion(Long clienteId) {
        if (!clientes.existsById(clienteId)) throw new ResourceNotFoundException("Cliente");
        var guardada = adaptaciones.findFirstByClienteIdOrderByFechaAplicacionDescIdDesc(clienteId).orElse(null);
        if (guardada != null) return leer(guardada);
        PrediccionModelo prediccion = ultimaEvaluacionDiaria(clienteId);
        if (prediccion == null) return OrientacionResponse.sinEvaluacion();

        return construir(prediccion, null);
    }

    @Transactional
    public void guardarParaPrediccion(PrediccionModelo prediccion, PlanDiario plan) {
        if (adaptaciones.findByPrediccionModeloId(prediccion.getId()).isPresent()) return;
        var respuesta = construir(prediccion, plan);
        if (!respuesta.personalizadaDisponible()) return;
        try {
            var entidad = new AdaptacionDiaria();
            entidad.setCliente(prediccion.getCliente());
            entidad.setPrediccionModelo(prediccion);
            entidad.setPlanDiario(plan);
            entidad.setFechaEvaluada(respuesta.evaluacion().fechaEvaluada());
            entidad.setFechaAplicacion(respuesta.fechaAplicacion());
            entidad.setContenidoJson(json.writeValueAsString(respuesta));
            adaptaciones.save(entidad);
        } catch (RuntimeException error) {
            throw error;
        } catch (Exception error) {
            throw new IllegalStateException("No se pudo guardar la orientación diaria", error);
        }
    }

    private OrientacionResponse construir(PrediccionModelo prediccion, PlanDiario plan) {

        var fechaEvaluada = prediccion.getFechaCorte().minusDays(1);
        var resumen = resumenes.resumen(prediccion.getCliente().getId(), fechaEvaluada);
        boolean diaCompleto = resumen.consumido().registrosAlimento() > 0
                && resumen.consumido().total().calculable()
                && resumen.agua().declarado()
                && "DISPONIBLE".equals(resumen.objetivo().estado());
        if (!diaCompleto) return OrientacionResponse.sinEvaluacion();

        List<OrientacionResponse.Comparacion> comparaciones = new ArrayList<>();
        comparaciones.add(comparar("KCAL", "Calorías", "kcal", resumen.objetivo().kcal(), resumen.consumido().total().kcal()));
        comparaciones.add(comparar("PROTEINA", "Proteínas", "g", resumen.objetivo().proteinaG(), resumen.consumido().total().proteinaG()));
        comparaciones.add(comparar("CARBOHIDRATOS", "Carbohidratos", "g", resumen.objetivo().carbohidratosG(), resumen.consumido().total().carbohidratosG()));
        comparaciones.add(comparar("GRASAS", "Grasas", "g", resumen.objetivo().grasasG(), resumen.consumido().total().grasasG()));
        comparaciones.add(comparar("AGUA", "Agua", "ml", resumen.agua().objetivoMl(),
                BigDecimal.valueOf(resumen.agua().consumidoMl())));

        List<OrientacionResponse.Comparacion> desviaciones = comparaciones.stream()
                .filter(c -> "BAJO".equals(c.estado()) || "ALTO".equals(c.estado()))
                .sorted(Comparator.comparing(this::desviacion).reversed())
                .toList();
        List<OrientacionResponse.Prioridad> prioridades = crearPrioridades(desviaciones);
        List<OrientacionResponse.Recomendacion> recomendaciones = agregarRecomendacionesSuplementacion(
                prediccion, crearRecomendaciones(comparaciones, desviaciones));

        var evaluacion = new OrientacionResponse.Evaluacion(
                prediccion.getId(), fechaEvaluada, prediccion.getClasificacionPredicha().name(),
                prediccion.getProbAdecuado(), prediccion.getProbMejorable(), prediccion.getProbCritico(),
                confianzaPct(prediccion), prediccion.getModelVersion(), prediccion.getInferenceMs(),
                prediccion.getInferredAt());
        return new OrientacionResponse(true, null, prediccion.getFechaCorte(),
                plan == null ? null : plan.getId(), evaluacion, comparaciones, prioridades, recomendaciones);
    }

    private OrientacionResponse leer(AdaptacionDiaria adaptacion) {
        try { return json.readValue(adaptacion.getContenidoJson(), OrientacionResponse.class); }
        catch (Exception error) { throw new IllegalStateException("No se pudo leer la orientación diaria guardada", error); }
    }

    private PrediccionModelo ultimaEvaluacionDiaria(Long clienteId) {
        return predicciones.findByClienteIdAndEstadoOrderByFechaPrediccionDesc(
                        clienteId, EstadoPrediccionModelo.EXITOSA).stream()
                .filter(p -> com.backend.nutri_predic.prediccionmodelo.service.ModeloPredictivoV6Service
                        .esModeloDiarioAdmitido(p.getModelVersion()))
                .filter(p -> p.getClasificacionPredicha() != null)
                .findFirst().orElse(null);
    }

    private OrientacionResponse.Comparacion comparar(
            String codigo, String nombre, String unidad, BigDecimal meta, BigDecimal consumido) {
        BigDecimal porcentaje = meta == null || meta.signum() <= 0 || consumido == null ? null
                : consumido.multiply(BigDecimal.valueOf(100)).divide(meta, 1, RoundingMode.HALF_UP);
        String estado = porcentaje == null ? "NO_CALCULABLE"
                : porcentaje.compareTo(LIMITE_BAJO) < 0 ? "BAJO"
                : porcentaje.compareTo(LIMITE_ALTO) > 0 ? "ALTO" : "EN_RANGO";
        BigDecimal diferencia = meta == null || consumido == null ? null : consumido.subtract(meta);
        String estadoNormalizado = switch (estado) {
            case "BAJO" -> "DEFICIT";
            case "EN_RANGO" -> "ADECUADO";
            case "ALTO" -> "EXCESO";
            default -> "NO_CALCULABLE";
        };
        return new OrientacionResponse.Comparacion(
                codigo, nombre, unidad, meta, consumido, diferencia, porcentaje, estado, estadoNormalizado);
    }

    private BigDecimal confianzaPct(PrediccionModelo prediccion) {
        return java.util.stream.Stream.of(
                        prediccion.getProbAdecuado(),
                        prediccion.getProbMejorable(),
                        prediccion.getProbCritico())
                .filter(java.util.Objects::nonNull)
                .max(BigDecimal::compareTo)
                .map(valor -> valor.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                .orElse(null);
    }

    private BigDecimal desviacion(OrientacionResponse.Comparacion comparacion) {
        return comparacion.porcentaje() == null ? BigDecimal.ZERO
                : comparacion.porcentaje().subtract(BigDecimal.valueOf(100)).abs();
    }

    private List<OrientacionResponse.Prioridad> crearPrioridades(
            List<OrientacionResponse.Comparacion> desviaciones) {
        if (desviaciones.isEmpty()) {
            return List.of(new OrientacionResponse.Prioridad(
                    "MANTENER_RANGO", "Mantener el equilibrio logrado",
                    "Las cinco metas evaluadas estuvieron dentro del rango de 80 % a 120 %.", BigDecimal.ZERO));
        }
        return desviaciones.stream().limit(3).map(c -> new OrientacionResponse.Prioridad(
                c.codigo() + "_" + c.estado(),
                c.nombre() + ("BAJO".equals(c.estado()) ? " por debajo de la meta" : " por encima de la meta"),
                formatoComparacion(c), desviacion(c))).toList();
    }

    private List<OrientacionResponse.Recomendacion> crearRecomendaciones(
            List<OrientacionResponse.Comparacion> comparaciones,
            List<OrientacionResponse.Comparacion> desviaciones) {
        List<OrientacionResponse.Recomendacion> resultado = new ArrayList<>();
        desviaciones.stream().limit(5).forEach(c -> resultado.add(recomendacionDesviacion(c)));
        for (var c : comparaciones) {
            if (resultado.size() >= 3) break;
            if ("EN_RANGO".equals(c.estado())) resultado.add(recomendacionMantenimiento(c));
        }
        if (resultado.size() < 3) {
            resultado.add(new OrientacionResponse.Recomendacion(
                    "REGISTRO_CONSTANTE", "Mantén un registro diario completo",
                    "Continúa registrando alimentos, macronutrientes y agua para comparar días consecutivos con la misma base.",
                    "CONTINUIDAD_DEL_REGISTRO"));
        }
        return new ArrayList<>(resultado.stream().limit(5).toList());
    }

    private List<OrientacionResponse.Recomendacion> agregarRecomendacionesSuplementacion(
            PrediccionModelo prediccion, List<OrientacionResponse.Recomendacion> base) {
        var resultado = new ArrayList<OrientacionResponse.Recomendacion>();
        var evaluacion = evaluacionesConsumo
                .findFirstByPrediccionModeloIdOrderByFechaEvaluacionDesc(prediccion.getId())
                .orElse(null);
        if (evaluacion != null) {
            detallesConsumo.findByEvaluacionIdOrderByIdAsc(evaluacion.getId()).stream()
                    .filter(detalle -> "CUMPLE".equals(detalle.getResultado()))
                    .filter(detalle -> "SUPLEMENTACION".equals(detalle.getFuente()))
                    .filter(detalle -> detalle.getComponente() != null
                            && detalle.getCantidadObservada() != null
                            && referencia(detalle) != null)
                    .limit(2)
                    .forEach(detalle -> resultado.add(recomendacionSuplementacion(detalle)));
        }
        base.stream().limit(5 - resultado.size()).forEach(resultado::add);
        return resultado.stream().limit(5).toList();
    }

    private OrientacionResponse.Recomendacion recomendacionSuplementacion(DetalleEvaluacionConsumo detalle) {
        BigDecimal referencia = referencia(detalle);
        String unidad = detalle.getUnidad() == null ? "" : " " + detalle.getUnidad();
        String componente = detalle.getComponente().replace('_', ' ').toLowerCase();
        String evidencia = detalle.getCantidadObservada().stripTrailingZeros().toPlainString() + unidad
                + " registrados; referencia aplicada: "
                + referencia.stripTrailingZeros().toPlainString() + unidad + ".";
        return new OrientacionResponse.Recomendacion(
                "REVISAR_SUPLEMENTACION_" + detalle.getComponente(),
                "Revisar el consumo de " + componente,
                "El criterio validado de suplementación detectó un consumo por encima de su referencia. "
                        + "Revisa la composición y la porción declaradas antes de añadir otra toma; si necesitas "
                        + "ajustarla, consulta a un profesional de salud.",
                evidencia);
    }

    private BigDecimal referencia(DetalleEvaluacionConsumo detalle) {
        return detalle.getReferenciaAplicadaHasta() != null
                ? detalle.getReferenciaAplicadaHasta()
                : detalle.getReferenciaAplicada();
    }

    private OrientacionResponse.Recomendacion recomendacionDesviacion(OrientacionResponse.Comparacion c) {
        boolean bajo = "BAJO".equals(c.estado());
        String descripcion = switch (c.codigo()) {
            case "KCAL" -> bajo
                    ? "Aumenta gradualmente las porciones de alimentos nutritivos hasta acercarte a tu meta energética."
                    : "Reduce gradualmente el tamaño de las porciones y prioriza alimentos de menor densidad energética."
                    ;
            case "PROTEINA" -> bajo
                    ? "Incluye una fuente de proteína en las comidas principales, como huevos, pescado, carnes magras, lácteos o legumbres."
                    : "Distribuye la proteína entre las comidas y evita añadir porciones extra cuando tu meta diaria ya esté cubierta."
                    ;
            case "CARBOHIDRATOS" -> bajo
                    ? "Añade fuentes de carbohidratos como avena, arroz, papa, quinua, frutas o legumbres según tus porciones objetivo."
                    : "Ajusta las porciones de cereales, tubérculos, bebidas azucaradas y otros carbohidratos concentrados."
                    ;
            case "GRASAS" -> bajo
                    ? "Incorpora porciones moderadas de grasas como palta, frutos secos, semillas o aceite vegetal."
                    : "Reduce frituras, salsas grasas y porciones grandes de aceites o frutos secos para acercarte a la meta."
                    ;
            case "AGUA" -> bajo
                    ? "Distribuye el agua durante el día y usa recordatorios hasta aproximarte a tu meta de hidratación."
                    : "Evita forzar líquidos por encima de tu meta y distribuye el consumo de manera uniforme durante el día."
                    ;
            default -> "Ajusta progresivamente este componente hasta acercarte a la meta registrada.";
        };
        return new OrientacionResponse.Recomendacion(
                "AJUSTAR_" + c.codigo() + "_" + c.estado(),
                (bajo ? "Aumentar " : "Reducir ") + c.nombre().toLowerCase(),
                descripcion, formatoComparacion(c));
    }

    private OrientacionResponse.Recomendacion recomendacionMantenimiento(OrientacionResponse.Comparacion c) {
        return new OrientacionResponse.Recomendacion(
                "MANTENER_" + c.codigo(), "Mantener " + c.nombre().toLowerCase(),
                "El consumo estuvo dentro del rango objetivo; conserva una distribución similar y continúa registrándolo.",
                formatoComparacion(c));
    }

    private String formatoComparacion(OrientacionResponse.Comparacion c) {
        return c.consumido().stripTrailingZeros().toPlainString() + " " + c.unidad()
                + " consumidos de " + c.meta().stripTrailingZeros().toPlainString() + " " + c.unidad()
                + " (" + c.porcentaje().stripTrailingZeros().toPlainString() + " %)";
    }
}
