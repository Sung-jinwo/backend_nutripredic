package com.backend.nutri_predic.perfilhabitos.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.perfilhabitos.dto.*;
import com.backend.nutri_predic.perfilhabitos.entity.*;
import com.backend.nutri_predic.perfilhabitos.repository.*;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluacionPerfilHabitosV6Service {
    private static final BigDecimal COBERTURA_MINIMA = new BigDecimal("70.00");
    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final CriterioClasificacionPerfilRepository criteriosClasificacion;
    private final CriterioRubricaPerfilHabitosRepository criteriosRubrica;
    private final DimensionRubricaPerfilHabitosRepository dimensionesRubrica;
    private final ResultadoDimensionPerfilHabitosRepository resultadosDimension;
    private final ResultadoCriterioPerfilHabitosRepository resultadosCriterio;
    private final EvidenciaPerfilHabitosV6Service evidenciaService;

    public EvaluacionPerfilHabitosV6Service(
            ClienteRepository clientes,
            UsuarioRepository usuarios,
            EvaluacionPerfilHabitosRepository evaluaciones,
            CriterioClasificacionPerfilRepository criteriosClasificacion,
            CriterioRubricaPerfilHabitosRepository criteriosRubrica,
            DimensionRubricaPerfilHabitosRepository dimensionesRubrica,
            ResultadoDimensionPerfilHabitosRepository resultadosDimension,
            ResultadoCriterioPerfilHabitosRepository resultadosCriterio,
            EvidenciaPerfilHabitosV6Service evidenciaService) {
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.evaluaciones = evaluaciones;
        this.criteriosClasificacion = criteriosClasificacion;
        this.criteriosRubrica = criteriosRubrica;
        this.dimensionesRubrica = dimensionesRubrica;
        this.resultadosDimension = resultadosDimension;
        this.resultadosCriterio = resultadosCriterio;
        this.evidenciaService = evidenciaService;
    }

    @Transactional
    public DetalleEvaluacionPerfilHabitosResponse crearDesdeEvidencia(
            Long clienteId,
            LocalDate fechaCorte,
            CrearEvaluacionPerfilHabitosV6Request request,
            Authentication authentication) {
        var evidencia = evidenciaService.calcular(clienteId, fechaCorte);
        var cliente =
                clientes.findById(clienteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var evaluador =
                usuarios.findByEmail(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Evaluador"));
        var rubricaId = evidencia.rubricaId();
        var manuales =
                Optional.ofNullable(request.criteriosManuales()).orElse(List.of()).stream()
                        .collect(Collectors.toMap(
                                x -> x.codigoCriterio().trim(),
                                Function.identity(),
                                (a, b) -> b));
        var criterios =
                criteriosRubrica.findByRubricaIdOrderByDimensionOrdenAscOrdenAsc(rubricaId);
        var evidenciaPorCodigo =
                evidencia.criterios().stream()
                        .collect(Collectors.toMap(
                                EvidenciaPerfilHabitosV6Response.EvidenciaCriterio::codigo,
                                Function.identity()));
        List<ResultadoCriterioPerfilHabitos> resultados = new ArrayList<>();
        for (var criterio : criterios) {
            resultados.add(resultadoCriterio(criterio, evidenciaPorCodigo.get(criterio.getCodigo()), manuales));
        }
        var dimensiones = dimensionesRubrica.findByRubricaIdOrderByOrdenAsc(rubricaId);
        var resultadosDim = resultadosDimensiones(dimensiones, resultados);
        BigDecimal maxCalculable =
                resultados.stream()
                        .map(ResultadoCriterioPerfilHabitos::getPuntosMaximos)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cobertura = porcentaje(maxCalculable, CIEN);
        BigDecimal total = puntajeNormalizado(resultadosDim);
        var evaluacion = new EvaluacionPerfilHabitos();
        evaluacion.setCliente(cliente);
        evaluacion.setRubrica(criterios.getFirst().getRubrica());
        evaluacion.setEvaluador(evaluador);
        evaluacion.setFechaCorte(fechaCorte);
        evaluacion.setPuntajeTotal(total);
        evaluacion.setPuntajeMaximoCalculable(maxCalculable);
        evaluacion.setCoberturaCalculable(cobertura);
        evaluacion.setMotivoNoValida(
                cobertura != null && cobertura.compareTo(COBERTURA_MINIMA) < 0
                        ? "COBERTURA_CALCULABLE_MENOR_70"
                        : null);
        evaluacion.setEstadoValidez(
                cobertura != null && cobertura.compareTo(COBERTURA_MINIMA) >= 0
                        ? EstadoValidezMedicion.VALIDA
                        : EstadoValidezMedicion.INVALIDA);
        evaluacion.setClasificacionReal(clasificar(rubricaId, total));
        evaluacion.setObservacion(texto(request.observacion()));
        var guardada = evaluaciones.save(evaluacion);
        resultados.forEach(r -> {
            r.setEvaluacion(guardada);
            resultadosCriterio.save(r);
        });
        resultadosDim.forEach(r -> {
            r.setEvaluacion(guardada);
            resultadosDimension.save(r);
        });
        return detalle(guardada.getId());
    }

    @Transactional(readOnly = true)
    public DetalleEvaluacionPerfilHabitosResponse detalle(Long evaluacionId) {
        var evaluacion =
                evaluaciones.findById(evaluacionId)
                        .orElseThrow(() -> new ResourceNotFoundException("Evaluación de perfil"));
        return new DetalleEvaluacionPerfilHabitosResponse(
                EvaluacionPerfilHabitosResponse.from(evaluacion),
                resultadosDimension.findByEvaluacionIdOrderByDimensionOrdenAsc(evaluacionId).stream()
                        .map(ResultadoDimensionPerfilHabitosResponse::from)
                        .toList(),
                resultadosCriterio
                        .findByEvaluacionIdOrderByCriterioDimensionOrdenAscCriterioOrdenAsc(
                                evaluacionId)
                        .stream()
                        .map(ResultadoCriterioPerfilHabitosResponse::from)
                        .toList());
    }

    private ResultadoCriterioPerfilHabitos resultadoCriterio(
            CriterioRubricaPerfilHabitos criterio,
            EvidenciaPerfilHabitosV6Response.EvidenciaCriterio evidencia,
            Map<String, PuntuacionManualCriterioRequest> manuales) {
        var resultado = new ResultadoCriterioPerfilHabitos();
        resultado.setCriterio(criterio);
        if (criterio.getTipoEvaluacion() == TipoEvaluacionCriterioPerfil.MANUAL_ESPECIALISTA) {
            var manual = manuales.get(criterio.getCodigo());
            if (manual == null) {
                resultado.setEstado(EstadoResultadoCriterioPerfil.MANUAL_PENDIENTE);
                resultado.setMotivoNoCalculable("Pendiente de puntuación manual del especialista");
                return resultado;
            }
            if (manual.puntosObtenidos().compareTo(criterio.getPuntosMaximos()) > 0) {
                throw new BusinessException(
                        "La puntuación manual supera el máximo del criterio "
                                + criterio.getCodigo());
            }
            resultado.setEstado(EstadoResultadoCriterioPerfil.CUMPLE);
            resultado.setPuntosObtenidos(escala(manual.puntosObtenidos()));
            resultado.setPuntosMaximos(criterio.getPuntosMaximos());
            resultado.setReferenciaAplicada(criterio.getReferencia());
            resultado.setObservacion(texto(manual.observacion()));
            return resultado;
        }
        if (evidencia == null) {
            resultado.setEstado(EstadoResultadoCriterioPerfil.NO_CALCULABLE);
            resultado.setMotivoNoCalculable("Sin evidencia calculada por backend");
            return resultado;
        }
        resultado.setEstado(EstadoResultadoCriterioPerfil.valueOf(evidencia.estado()));
        resultado.setValorObservado(evidencia.valorObservado());
        resultado.setPuntosObtenidos(evidencia.puntosObtenidos());
        resultado.setPuntosMaximos(evidencia.puntosMaximos());
        resultado.setReferenciaAplicada(evidencia.referenciaAplicada());
        resultado.setMotivoNoCalculable(texto(evidencia.motivoNoCalculable()));
        return resultado;
    }

    private List<ResultadoDimensionPerfilHabitos> resultadosDimensiones(
            List<DimensionRubricaPerfilHabitos> dimensiones,
            List<ResultadoCriterioPerfilHabitos> criterios) {
        List<ResultadoDimensionPerfilHabitos> out = new ArrayList<>();
        for (var dimension : dimensiones) {
            var propios =
                    criterios.stream()
                            .filter(c -> c.getCriterio().getDimension().getId().equals(dimension.getId()))
                            .toList();
            BigDecimal obtenidoRaw = suma(propios, ResultadoCriterioPerfilHabitos::getPuntosObtenidos);
            BigDecimal maxRaw = suma(propios, ResultadoCriterioPerfilHabitos::getPuntosMaximos);
            BigDecimal obtenidoNormalizado =
                    maxRaw.signum() == 0
                            ? BigDecimal.ZERO
                            : obtenidoRaw.multiply(dimension.getPesoPorcentual())
                                    .divide(maxRaw, 2, RoundingMode.HALF_UP);
            var resultado = new ResultadoDimensionPerfilHabitos();
            resultado.setDimension(dimension);
            resultado.setPuntosObtenidos(obtenidoNormalizado);
            resultado.setPuntosMaximosCalculables(maxRaw);
            resultado.setCoberturaCalculable(porcentaje(maxRaw, dimension.getPesoPorcentual()));
            resultado.setCriteriosNoCalculables(
                    propios.stream()
                            .filter(c -> c.getPuntosMaximos() == null)
                            .map(c -> c.getCriterio().getCodigo() + ":" + c.getEstado().name())
                            .collect(Collectors.joining("; ")));
            out.add(resultado);
        }
        return out;
    }

    private BigDecimal puntajeNormalizado(List<ResultadoDimensionPerfilHabitos> dimensiones) {
        return dimensiones.stream()
                .map(ResultadoDimensionPerfilHabitos::getPuntosObtenidos)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private ClasificacionPerfilHabitos clasificar(Long rubricaId, BigDecimal puntaje) {
        var aplicables =
                criteriosClasificacion.findByRubricaIdOrderByOrdenAsc(rubricaId).stream()
                        .filter(c -> aplica(c, puntaje))
                        .map(CriterioClasificacionPerfil::getClasificacion)
                        .toList();
        if (aplicables.size() != 1) {
            throw new BusinessException("La rúbrica no determina una clasificación única");
        }
        return aplicables.getFirst();
    }

    private boolean aplica(CriterioClasificacionPerfil c, BigDecimal puntaje) {
        int inferior = puntaje.compareTo(c.getLimiteInferior());
        int superior = puntaje.compareTo(c.getLimiteSuperior());
        return (inferior > 0 || inferior == 0 && c.isIncluyeInferior())
                && (superior < 0 || superior == 0 && c.isIncluyeSuperior());
    }

    private BigDecimal suma(
            List<ResultadoCriterioPerfilHabitos> criterios,
            Function<ResultadoCriterioPerfilHabitos, BigDecimal> getter) {
        return criterios.stream().map(getter).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal porcentaje(BigDecimal parte, BigDecimal total) {
        return parte == null || total == null || total.signum() <= 0
                ? null
                : parte.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal escala(BigDecimal valor) {
        return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String texto(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
