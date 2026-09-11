package com.backend.nutri_predic.consumo.service;

import com.backend.nutri_predic.common.exception.BusinessException;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.consumo.dto.CambioEstadoRubricaConsumoRequest;
import com.backend.nutri_predic.consumo.dto.CriterioConsumoSuplementosRequest;
import com.backend.nutri_predic.consumo.dto.CriterioConsumoSuplementosResponse;
import com.backend.nutri_predic.consumo.dto.RubricaConsumoSuplementosRequest;
import com.backend.nutri_predic.consumo.dto.RubricaConsumoSuplementosResponse;
import com.backend.nutri_predic.consumo.entity.CriterioConsumoSuplementos;
import com.backend.nutri_predic.consumo.entity.EstadoCriterioConsumo;
import com.backend.nutri_predic.consumo.entity.RubricaConsumoSuplementos;
import com.backend.nutri_predic.consumo.repository.CriterioConsumoSuplementosRepository;
import com.backend.nutri_predic.consumo.repository.RubricaConsumoSuplementosRepository;
import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;
import com.backend.nutri_predic.suplemento.repository.SuplementoCatalogoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RubricaConsumoSuplementosService {
    private final RubricaConsumoSuplementosRepository rubricas;
    private final CriterioConsumoSuplementosRepository criterios;
    private final SuplementoCatalogoRepository suplementos;

    public RubricaConsumoSuplementosService(
            RubricaConsumoSuplementosRepository rubricas,
            CriterioConsumoSuplementosRepository criterios,
            SuplementoCatalogoRepository suplementos) {
        this.rubricas = rubricas;
        this.criterios = criterios;
        this.suplementos = suplementos;
    }

    @Transactional
    public RubricaConsumoSuplementosResponse crear(RubricaConsumoSuplementosRequest request) {
        validarVigencia(request);
        String codigo = request.codigo().trim();
        if (rubricas.existsByCodigoAndVersion(codigo, request.version())) {
            throw new BusinessException("Ya existe la versión indicada para la rúbrica PCS");
        }
        var rubrica = new RubricaConsumoSuplementos();
        rubrica.setEstado(EstadoCriterioConsumo.BORRADOR);
        aplicar(rubrica, request);
        return respuesta(rubricas.save(rubrica));
    }

    @Transactional(readOnly = true)
    public List<RubricaConsumoSuplementosResponse> listar() {
        return rubricas.findAllByOrderByVersionDescIdDesc().stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public RubricaConsumoSuplementosResponse obtener(Long id) {
        return respuesta(rubrica(id));
    }

    @Transactional
    public RubricaConsumoSuplementosResponse actualizar(
            Long id, RubricaConsumoSuplementosRequest request) {
        validarVigencia(request);
        var rubrica = rubrica(id);
        String codigo = request.codigo().trim();
        if (rubricas.existsByCodigoAndVersionAndIdNot(codigo, request.version(), id)) {
            throw new BusinessException("Ya existe la versión indicada para la rúbrica PCS");
        }
        if (rubrica.getEstado() == EstadoCriterioConsumo.ACTIVO
                && !Boolean.TRUE.equals(request.validada())) {
            throw new BusinessException("Una rúbrica activa debe permanecer validada");
        }
        aplicar(rubrica, request);
        return respuesta(rubricas.save(rubrica));
    }

    @Transactional
    public RubricaConsumoSuplementosResponse cambiarEstado(
            Long id, CambioEstadoRubricaConsumoRequest request) {
        var rubrica = rubrica(id);
        if (request.estado() == EstadoCriterioConsumo.ACTIVO && !rubrica.isValidada()) {
            throw new BusinessException("No se puede activar una rúbrica PCS no validada");
        }
        rubrica.setEstado(request.estado());
        return respuesta(rubricas.save(rubrica));
    }

    @Transactional
    public CriterioConsumoSuplementosResponse agregarCriterio(
            Long rubricaId, CriterioConsumoSuplementosRequest request) {
        var criterio = new CriterioConsumoSuplementos();
        criterio.setRubrica(rubrica(rubricaId));
        aplicar(criterio, request);
        return CriterioConsumoSuplementosResponse.from(criterios.save(criterio));
    }

    @Transactional
    public CriterioConsumoSuplementosResponse actualizarCriterio(
            Long rubricaId, Long criterioId, CriterioConsumoSuplementosRequest request) {
        rubrica(rubricaId);
        var criterio = criterio(rubricaId, criterioId);
        aplicar(criterio, request);
        return CriterioConsumoSuplementosResponse.from(criterios.save(criterio));
    }

    @Transactional
    public void eliminarCriterio(Long rubricaId, Long criterioId) {
        rubrica(rubricaId);
        criterios.delete(criterio(rubricaId, criterioId));
    }

    private void aplicar(
            RubricaConsumoSuplementos rubrica, RubricaConsumoSuplementosRequest request) {
        rubrica.setCodigo(request.codigo().trim());
        rubrica.setVersion(request.version());
        rubrica.setVentanaDias(request.ventanaDias());
        rubrica.setVigenteDesde(request.vigenteDesde());
        rubrica.setVigenteHasta(request.vigenteHasta());
        rubrica.setValidada(Boolean.TRUE.equals(request.validada()));
        rubrica.setObservacion(texto(request.observacion()));
        rubrica.setFuenteReferencia(texto(request.fuenteReferencia()));
        rubrica.setValidadoPor(texto(request.validadoPor()));
        rubrica.setValidadoEn(request.validadoEn());
        rubrica.setVersionEvaluador(texto(request.versionEvaluador()));
        rubrica.setMetadataValidacion(texto(request.metadataValidacion()));
        rubrica.setReglaGlobal(texto(request.reglaGlobal()));
    }

    private void aplicar(
            CriterioConsumoSuplementos criterio, CriterioConsumoSuplementosRequest request) {
        criterio.setAlcance(request.alcance().trim());
        criterio.setSuplemento(suplemento(request.suplementoId()));
        criterio.setComponenteTipo(texto(request.componenteTipo()));
        criterio.setElementoAplicable(texto(request.elementoAplicable()));
        criterio.setMetrica(texto(request.metrica()));
        criterio.setCantidadReferencia(request.cantidadReferencia());
        criterio.setCantidadReferenciaHasta(request.cantidadReferenciaHasta());
        criterio.setAmbitoAporte(request.ambitoAporte());
        criterio.setUnidadReferencia(texto(request.unidadReferencia()));
        criterio.setUnidadNormalizada(texto(request.unidadNormalizada()));
        criterio.setFrecuencia(texto(request.frecuencia()));
        criterio.setNumeroTomas(request.numeroTomas());
        criterio.setVentanaDias(request.ventanaDias());
        criterio.setRequiereComposicion(request.requiereComposicion());
        criterio.setOperador(request.operador());
        criterio.setParametros(texto(request.parametros()));
        criterio.setFuenteReferencia(texto(request.fuenteReferencia()));
        criterio.setVersionReferencia(texto(request.versionReferencia()));
        criterio.setObservacionMetodologica(texto(request.observacionMetodologica()));
    }

    private void validarVigencia(RubricaConsumoSuplementosRequest request) {
        if (request.vigenteDesde() != null
                && request.vigenteHasta() != null
                && request.vigenteHasta().isBefore(request.vigenteDesde())) {
            throw new BusinessException("vigenteHasta no puede ser anterior a vigenteDesde");
        }
    }

    private RubricaConsumoSuplementos rubrica(Long id) {
        return rubricas.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rúbrica PCS"));
    }

    private CriterioConsumoSuplementos criterio(Long rubricaId, Long criterioId) {
        return criterios
                .findByIdAndRubricaId(criterioId, rubricaId)
                .orElseThrow(() -> new ResourceNotFoundException("Criterio de rúbrica PCS"));
    }

    private SuplementoCatalogo suplemento(Long id) {
        if (id == null) {
            return null;
        }
        return suplementos
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suplemento"));
    }

    private RubricaConsumoSuplementosResponse respuesta(RubricaConsumoSuplementos rubrica) {
        var lista =
                criterios.findByRubricaIdOrderByIdAsc(rubrica.getId()).stream()
                        .map(CriterioConsumoSuplementosResponse::from)
                        .toList();
        return RubricaConsumoSuplementosResponse.from(rubrica, lista);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
