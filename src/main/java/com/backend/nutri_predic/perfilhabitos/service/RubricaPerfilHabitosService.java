package com.backend.nutri_predic.perfilhabitos.service;

import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.perfilhabitos.dto.*;
import com.backend.nutri_predic.perfilhabitos.entity.*;
import com.backend.nutri_predic.perfilhabitos.repository.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RubricaPerfilHabitosService {
    private final RubricaPerfilHabitosRepository rubricas;
    private final CriterioClasificacionPerfilRepository criterios;
    private final DimensionRubricaPerfilHabitosRepository dimensiones;
    private final CriterioRubricaPerfilHabitosRepository criteriosEvaluacion;

    public RubricaPerfilHabitosService(
            RubricaPerfilHabitosRepository rubricas,
            CriterioClasificacionPerfilRepository criterios,
            DimensionRubricaPerfilHabitosRepository dimensiones,
            CriterioRubricaPerfilHabitosRepository criteriosEvaluacion) {
        this.rubricas = rubricas;
        this.criterios = criterios;
        this.dimensiones = dimensiones;
        this.criteriosEvaluacion = criteriosEvaluacion;
    }

    @Transactional
    public RubricaPerfilHabitosResponse crear(RubricaPerfilHabitosRequest request) {
        RubricaPerfilHabitos r = new RubricaPerfilHabitos();
        r.setCodigo(request.codigo().trim());
        r.setVersion(request.version());
        r.setNombre(request.nombre().trim());
        r.setDescripcion(texto(request.descripcion()));
        r.setVigenteDesde(request.vigenteDesde());
        r.setVigenteHasta(request.vigenteHasta());
        r.setValidadoPor(texto(request.validadoPor()));
        r.setValidadoEn(request.validadoEn());
        r.setObservacionValidacion(texto(request.observacionValidacion()));
        validarMetadatosValidacion(r);
        return respuesta(rubricas.save(r));
    }

    @Transactional(readOnly = true)
    public List<RubricaPerfilHabitosResponse> listar() {
        return rubricas.findAllByOrderByCodigoAscVersionDesc().stream()
                .map(this::respuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public RubricaPerfilHabitosResponse obtener(Long id) {
        return respuesta(rubrica(id));
    }

    @Transactional
    public CriterioClasificacionPerfilResponse agregarCriterio(
            Long rubricaId, CriterioClasificacionPerfilRequest request) {
        RubricaPerfilHabitos r = rubrica(rubricaId);
        exigirBorrador(r);
        if (request.limiteInferior().compareTo(request.limiteSuperior()) > 0)
            throw new BusinessException("El límite inferior no puede superar al superior");
        CriterioClasificacionPerfil c = new CriterioClasificacionPerfil();
        c.setRubrica(r);
        c.setClasificacion(request.clasificacion());
        c.setLimiteInferior(request.limiteInferior());
        c.setLimiteSuperior(request.limiteSuperior());
        c.setIncluyeInferior(request.incluyeInferior());
        c.setIncluyeSuperior(request.incluyeSuperior());
        c.setOrden(request.orden());
        return CriterioClasificacionPerfilResponse.from(criterios.save(c));
    }

    @Transactional(readOnly = true)
    public List<CriterioClasificacionPerfilResponse> criterios(Long rubricaId) {
        rubrica(rubricaId);
        return criterios.findByRubricaIdOrderByOrdenAsc(rubricaId).stream()
                .map(CriterioClasificacionPerfilResponse::from)
                .toList();
    }

    @Transactional
    public DimensionRubricaPerfilHabitosResponse agregarDimension(
            Long rubricaId, DimensionRubricaPerfilHabitosRequest request) {
        RubricaPerfilHabitos rubrica = rubrica(rubricaId);
        exigirBorrador(rubrica);
        DimensionRubricaPerfilHabitos dimension = new DimensionRubricaPerfilHabitos();
        dimension.setRubrica(rubrica);
        aplicar(dimension, request);
        return DimensionRubricaPerfilHabitosResponse.from(dimensiones.save(dimension));
    }

    @Transactional
    public DimensionRubricaPerfilHabitosResponse actualizarDimension(
            Long rubricaId, Long dimensionId, DimensionRubricaPerfilHabitosRequest request) {
        RubricaPerfilHabitos rubrica = rubrica(rubricaId);
        exigirBorrador(rubrica);
        DimensionRubricaPerfilHabitos dimension =
                dimensiones
                        .findById(dimensionId)
                        .filter(d -> d.getRubrica().getId().equals(rubricaId))
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Dimensión de la rúbrica"));
        aplicar(dimension, request);
        return DimensionRubricaPerfilHabitosResponse.from(dimensiones.save(dimension));
    }

    @Transactional(readOnly = true)
    public List<DimensionRubricaPerfilHabitosResponse> dimensiones(Long rubricaId) {
        rubrica(rubricaId);
        return dimensiones.findByRubricaIdOrderByOrdenAsc(rubricaId).stream()
                .map(DimensionRubricaPerfilHabitosResponse::from)
                .toList();
    }

    @Transactional
    public RubricaPerfilHabitosResponse registrarValidacion(
            Long rubricaId, ValidacionRubricaPerfilHabitosRequest request) {
        RubricaPerfilHabitos rubrica = rubrica(rubricaId);
        exigirBorrador(rubrica);
        rubrica.setValidadoPor(request.validadoPor().trim());
        rubrica.setValidadoEn(request.validadoEn());
        rubrica.setObservacionValidacion(texto(request.observacionValidacion()));
        rubrica.setVigenteDesde(request.vigenteDesde());
        rubrica.setVigenteHasta(request.vigenteHasta());
        validarMetadatosValidacion(rubrica);
        return respuesta(rubricas.save(rubrica));
    }

    @Transactional
    public RubricaPerfilHabitosResponse cambiarEstado(Long id, CambioEstadoRubricaRequest request) {
        RubricaPerfilHabitos r = rubrica(id);
        if (r.getEstado() != EstadoRubricaPerfilHabitos.BORRADOR)
            throw new BusinessException("La versión de rúbrica ya es inmutable");
        if (request.estado() == EstadoRubricaPerfilHabitos.ACTIVA) validarActivacion(r);
        r.setEstado(request.estado());
        return respuesta(rubricas.save(r));
    }

    private void validarActivacion(RubricaPerfilHabitos r) {
        if (r.getValidadoPor() == null || r.getValidadoEn() == null || r.getVigenteDesde() == null)
            throw new BusinessException("Una rúbrica activa debe estar validada");
        validarMetadatosValidacion(r);
        List<CriterioClasificacionPerfil> lista =
                criterios.findByRubricaIdOrderByOrdenAsc(r.getId());
        if (lista.size() != ClasificacionPerfilHabitos.values().length)
            throw new BusinessException(
                    "La rúbrica activa requiere los tres criterios de clasificación");
        BigDecimal esperado = BigDecimal.ZERO;
        boolean incluyeEsperado = true;
        for (CriterioClasificacionPerfil c : lista) {
            if (c.getLimiteInferior().compareTo(esperado) != 0
                    || c.isIncluyeInferior() != incluyeEsperado
                    || c.getLimiteSuperior().compareTo(c.getLimiteInferior()) < 0)
                throw new BusinessException(
                        "Los criterios deben cubrir el rango 0–100 sin huecos ni solapamientos");
            esperado = c.getLimiteSuperior();
            incluyeEsperado = !c.isIncluyeSuperior();
        }
        if (esperado.compareTo(new BigDecimal("100.00")) != 0 || incluyeEsperado)
            throw new BusinessException(
                    "Los criterios deben cubrir el rango 0–100 sin huecos ni solapamientos");
    }

    private RubricaPerfilHabitos rubrica(Long id) {
        return rubricas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rúbrica de perfil"));
    }

    private void exigirBorrador(RubricaPerfilHabitos r) {
        if (r.getEstado() != EstadoRubricaPerfilHabitos.BORRADOR)
            throw new BusinessException("La rúbrica debe permanecer en estado BORRADOR");
    }

    private void aplicar(
            DimensionRubricaPerfilHabitos dimension, DimensionRubricaPerfilHabitosRequest request) {
        if (request.puntajeMinimo() != null
                && request.puntajeMaximo() != null
                && request.puntajeMinimo().compareTo(request.puntajeMaximo()) > 0) {
            throw new BusinessException("El puntaje mínimo no puede superar al máximo");
        }
        dimension.setCodigo(request.codigo());
        dimension.setNombre(request.nombre().trim());
        dimension.setDescripcion(texto(request.descripcion()));
        dimension.setPuntajeMinimo(request.puntajeMinimo());
        dimension.setPuntajeMaximo(request.puntajeMaximo());
        dimension.setPesoPorcentual(request.pesoPorcentual());
        dimension.setOrden(request.orden());
    }

    private void validarMetadatosValidacion(RubricaPerfilHabitos rubrica) {
        boolean tieneValidador = rubrica.getValidadoPor() != null;
        boolean tieneFechaValidacion = rubrica.getValidadoEn() != null;
        if (tieneValidador != tieneFechaValidacion) {
            throw new BusinessException("validadoPor y validadoEn deben registrarse conjuntamente");
        }
        if (rubrica.getVigenteDesde() != null
                && rubrica.getVigenteHasta() != null
                && rubrica.getVigenteHasta().isBefore(rubrica.getVigenteDesde())) {
            throw new BusinessException("vigenteHasta no puede ser anterior a vigenteDesde");
        }
    }

    private RubricaPerfilHabitosResponse respuesta(RubricaPerfilHabitos r) {
        return RubricaPerfilHabitosResponse.from(
                r,
                criterios.findByRubricaIdOrderByOrdenAsc(r.getId()).stream()
                        .map(CriterioClasificacionPerfilResponse::from)
                        .toList(),
                dimensiones.findByRubricaIdOrderByOrdenAsc(r.getId()).stream()
                        .map(DimensionRubricaPerfilHabitosResponse::from)
                        .toList(),
                criteriosEvaluacion.findByRubricaIdOrderByDimensionOrdenAscOrdenAsc(r.getId()).stream()
                        .map(CriterioRubricaPerfilHabitosResponse::from)
                        .toList());
    }

    private String texto(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
