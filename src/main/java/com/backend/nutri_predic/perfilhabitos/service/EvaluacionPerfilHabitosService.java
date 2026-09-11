package com.backend.nutri_predic.perfilhabitos.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.perfilhabitos.dto.*;
import com.backend.nutri_predic.perfilhabitos.entity.*;
import com.backend.nutri_predic.perfilhabitos.repository.*;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluacionPerfilHabitosService {
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final RubricaPerfilHabitosRepository rubricas;
    private final CriterioClasificacionPerfilRepository criterios;
    private final DimensionRubricaPerfilHabitosRepository dimensiones;
    private final CriterioRubricaPerfilHabitosRepository criteriosRubrica;
    private final ResultadoDimensionPerfilHabitosRepository resultadosDimension;
    private final ResultadoCriterioPerfilHabitosRepository resultadosCriterio;
    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;

    public EvaluacionPerfilHabitosService(
            EvaluacionPerfilHabitosRepository e,
            RubricaPerfilHabitosRepository r,
            CriterioClasificacionPerfilRepository c,
            DimensionRubricaPerfilHabitosRepository dimensiones,
            CriterioRubricaPerfilHabitosRepository criteriosRubrica,
            ResultadoDimensionPerfilHabitosRepository resultadosDimension,
            ResultadoCriterioPerfilHabitosRepository resultadosCriterio,
            ClienteRepository clientes,
            UsuarioRepository usuarios) {
        this.evaluaciones = e;
        this.rubricas = r;
        this.criterios = c;
        this.dimensiones = dimensiones;
        this.criteriosRubrica = criteriosRubrica;
        this.resultadosDimension = resultadosDimension;
        this.resultadosCriterio = resultadosCriterio;
        this.clientes = clientes;
        this.usuarios = usuarios;
    }

    @Transactional
    public EvaluacionPerfilHabitosResponse crear(
            EvaluacionPerfilHabitosRequest request, Authentication authentication) {
        var cliente =
                clientes.findById(request.clienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var rubrica =
                rubricas.findById(request.rubricaId())
                        .orElseThrow(() -> new ResourceNotFoundException("Rúbrica de perfil"));
        if (rubrica.getEstado() != EstadoRubricaPerfilHabitos.ACTIVA)
            throw new BusinessException("Sólo se puede evaluar con una rúbrica ACTIVA");
        if (rubrica.getValidadoPor() == null
                || rubrica.getValidadoEn() == null
                || rubrica.getVigenteDesde() == null)
            throw new BusinessException("La rúbrica activa debe estar validada y vigente");
        var zona = ZoneId.systemDefault();
        var vigenteDesde = rubrica.getVigenteDesde().atZone(zona).toLocalDate();
        var vigenteHasta =
                rubrica.getVigenteHasta() == null
                        ? null
                        : rubrica.getVigenteHasta().atZone(zona).toLocalDate();
        if (request.fechaCorte().isBefore(vigenteDesde)
                || vigenteHasta != null && request.fechaCorte().isAfter(vigenteHasta))
            throw new BusinessException("La rúbrica no está vigente para la fecha de corte");
        if (request.puntajeTotal().compareTo(BigDecimal.ZERO) < 0
                || request.puntajeTotal().compareTo(new BigDecimal("100.00")) > 0)
            throw new BusinessException("El puntaje total debe estar entre 0 y 100");
        ClasificacionPerfilHabitos clasificacion =
                clasificar(rubrica.getId(), request.puntajeTotal());
        var evaluador =
                usuarios.findByEmail(authentication.getName())
                        .orElseThrow(() -> new ResourceNotFoundException("Evaluador"));
        EvaluacionPerfilHabitos e = new EvaluacionPerfilHabitos();
        e.setCliente(cliente);
        e.setRubrica(rubrica);
        e.setEvaluador(evaluador);
        e.setFechaCorte(request.fechaCorte());
        e.setPuntajeTotal(request.puntajeTotal());
        e.setPuntajeMaximoCalculable(request.puntajeMaximoCalculable());
        e.setCoberturaCalculable(request.coberturaCalculable());
        e.setMotivoNoValida(texto(request.motivoNoValida()));
        e.setClasificacionReal(clasificacion);
        e.setEstadoValidez(request.estadoValidez());
        e.setObservacion(texto(request.observacion()));
        var guardada = evaluaciones.save(e);
        guardarResultadosDimensiones(guardada, request);
        guardarResultadosCriterios(guardada, request);
        return EvaluacionPerfilHabitosResponse.from(guardada);
    }

    @Transactional(readOnly = true)
    public List<EvaluacionPerfilHabitosResponse> listar() {
        return evaluaciones.findAllByOrderByIdAsc().stream()
                .map(EvaluacionPerfilHabitosResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EvaluacionPerfilHabitosResponse obtener(Long id) {
        return EvaluacionPerfilHabitosResponse.from(
                evaluaciones
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Evaluación de perfil")));
    }

    private ClasificacionPerfilHabitos clasificar(Long rubricaId, BigDecimal puntaje) {
        List<ClasificacionPerfilHabitos> aplicables =
                criterios.findByRubricaIdOrderByOrdenAsc(rubricaId).stream()
                        .filter(c -> aplica(c, puntaje))
                        .map(CriterioClasificacionPerfil::getClasificacion)
                        .toList();
        if (aplicables.size() != 1)
            throw new BusinessException(
                    "La rúbrica no determina una clasificación única para el puntaje");
        return aplicables.getFirst();
    }

    private boolean aplica(CriterioClasificacionPerfil c, BigDecimal puntaje) {
        int inferior = puntaje.compareTo(c.getLimiteInferior());
        int superior = puntaje.compareTo(c.getLimiteSuperior());
        return (inferior > 0 || inferior == 0 && c.isIncluyeInferior())
                && (superior < 0 || superior == 0 && c.isIncluyeSuperior());
    }

    private String texto(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private void guardarResultadosDimensiones(
            EvaluacionPerfilHabitos evaluacion, EvaluacionPerfilHabitosRequest request) {
        if (request.dimensiones() == null) return;
        for (var item : request.dimensiones()) {
            var dimension =
                    dimensiones
                            .findById(item.dimensionId())
                            .filter(d -> d.getRubrica().getId().equals(evaluacion.getRubrica().getId()))
                            .orElseThrow(
                                    () ->
                                            new BusinessException(
                                                    "La dimensión no pertenece a la rúbrica evaluada"));
            var resultado = new ResultadoDimensionPerfilHabitos();
            resultado.setEvaluacion(evaluacion);
            resultado.setDimension(dimension);
            resultado.setPuntosObtenidos(item.puntosObtenidos());
            resultado.setPuntosMaximosCalculables(item.puntosMaximosCalculables());
            resultado.setCoberturaCalculable(item.coberturaCalculable());
            resultado.setCriteriosNoCalculables(texto(item.criteriosNoCalculables()));
            resultado.setObservacion(texto(item.observacion()));
            resultadosDimension.save(resultado);
        }
    }

    private void guardarResultadosCriterios(
            EvaluacionPerfilHabitos evaluacion, EvaluacionPerfilHabitosRequest request) {
        if (request.criterios() == null) return;
        for (var item : request.criterios()) {
            var criterio =
                    criteriosRubrica
                            .findById(item.criterioId())
                            .filter(c -> c.getRubrica().getId().equals(evaluacion.getRubrica().getId()))
                            .orElseThrow(
                                    () ->
                                            new BusinessException(
                                                    "El criterio no pertenece a la rúbrica evaluada"));
            var resultado = new ResultadoCriterioPerfilHabitos();
            resultado.setEvaluacion(evaluacion);
            resultado.setCriterio(criterio);
            resultado.setEstado(item.estado());
            resultado.setValorObservado(item.valorObservado());
            resultado.setPuntosObtenidos(item.puntosObtenidos());
            resultado.setPuntosMaximos(item.puntosMaximos());
            resultado.setReferenciaAplicada(texto(item.referenciaAplicada()));
            resultado.setMotivoNoCalculable(texto(item.motivoNoCalculable()));
            resultado.setObservacion(texto(item.observacion()));
            resultadosCriterio.save(resultado);
        }
    }
}
