package com.backend.nutri_predic.conocimiento.practica.entity;

import com.backend.nutri_predic.conocimiento.entity.InstrumentoConocimiento;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.plandia.entity.PlanDiario;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(
        name = "sesiones_conocimiento_ia",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"prediccion_modelo_id", "configuracion_version"}))
public class SesionConocimientoIa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prediccion_modelo_id")
    private PrediccionModelo prediccionModelo;

    @ManyToOne
    @JoinColumn(name = "reintento_de_sesion_id")
    private SesionConocimientoIa reintentoDeSesion;

    @ManyToOne
    @JoinColumn(name = "instrumento_id")
    private InstrumentoConocimiento instrumento;

    @ManyToOne
    @JoinColumn(name = "plan_diario_id")
    private PlanDiario planDiario;

    @Column(nullable = false)
    private String configuracionVersion;

    @Column(nullable = false)
    private String modelVersionPredictivo;

    @Column(nullable = false)
    private String schemaVersion;

    @Column(nullable = false)
    private String proveedorIa = "GEMINI";

    @Column(nullable = false)
    private String modeloGenerativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSesionConocimientoIa estado;

    private Instant generadoEn;
    private Instant respondidaEn;

    private LocalDate fechaEvaluacion;

    @Column(name = "cliente_id")
    private Long clienteId;

    @Column(length = 160)
    private String objetivoCliente;

    @Column(length = 40)
    private String clasificacionPredictiva;

    @Column(name = "meta_kcal", precision = 19, scale = 2)
    private BigDecimal metaKcal;
    @Column(name = "meta_proteina_g", precision = 19, scale = 2)
    private BigDecimal metaProteinaG;
    @Column(name = "meta_carbohidratos_g", precision = 19, scale = 2)
    private BigDecimal metaCarbohidratosG;
    @Column(name = "meta_grasas_g", precision = 19, scale = 2)
    private BigDecimal metaGrasasG;
    @Column(name = "meta_agua_ml", precision = 19, scale = 2)
    private BigDecimal metaAguaMl;

    @Column(precision = 5, scale = 2)
    private BigDecimal puntajeObtenido;

    @Column(precision = 5, scale = 2)
    private BigDecimal puntajeMaximo;

    @Column(length = 20)
    private String nivelResultado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validez", nullable = false, length = 30)
    private EstadoValidezMedicion estadoValidez = EstadoValidezMedicion.NO_DETERMINADA;

    @Column(name = "motivo_invalidez", length = 255)
    private String motivoInvalidez;
    private String codigoErrorTecnico;
    private Integer httpStatusGemini;
    private Instant fallidoEn;
    private String etapaError;
    private String tipoExcepcionSeguro;

    @Column(nullable = false, updatable = false)
    private Instant creadoEn = Instant.now();

    public Long getId() {
        return id;
    }

    public PrediccionModelo getPrediccionModelo() {
        return prediccionModelo;
    }

    public SesionConocimientoIa getReintentoDeSesion() {
        return reintentoDeSesion;
    }

    public InstrumentoConocimiento getInstrumento() {
        return instrumento;
    }
    public PlanDiario getPlanDiario() { return planDiario; }

    public String getConfiguracionVersion() {
        return configuracionVersion;
    }

    public String getModelVersionPredictivo() {
        return modelVersionPredictivo;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getProveedorIa() {
        return proveedorIa;
    }

    public String getModeloGenerativo() {
        return modeloGenerativo;
    }

    public EstadoSesionConocimientoIa getEstado() {
        return estado;
    }

    public Instant getGeneradoEn() {
        return generadoEn;
    }

    public Instant getRespondidaEn() {
        return respondidaEn;
    }

    public LocalDate getFechaEvaluacion() { return fechaEvaluacion; }
    public Long getClienteId() { return clienteId; }
    public String getObjetivoCliente() { return objetivoCliente; }
    public String getClasificacionPredictiva() { return clasificacionPredictiva; }
    public BigDecimal getMetaKcal() { return metaKcal; }
    public BigDecimal getMetaProteinaG() { return metaProteinaG; }
    public BigDecimal getMetaCarbohidratosG() { return metaCarbohidratosG; }
    public BigDecimal getMetaGrasasG() { return metaGrasasG; }
    public BigDecimal getMetaAguaMl() { return metaAguaMl; }
    public BigDecimal getPuntajeObtenido() { return puntajeObtenido; }
    public BigDecimal getPuntajeMaximo() { return puntajeMaximo; }
    public String getNivelResultado() { return nivelResultado; }
    public EstadoValidezMedicion getEstadoValidez() { return estadoValidez; }
    public String getMotivoInvalidez() { return motivoInvalidez; }

    public String getCodigoErrorTecnico() {
        return codigoErrorTecnico;
    }

    public Integer getHttpStatusGemini() {
        return httpStatusGemini;
    }

    public Instant getFallidoEn() {
        return fallidoEn;
    }

    public String getEtapaError() {
        return etapaError;
    }

    public String getTipoExcepcionSeguro() {
        return tipoExcepcionSeguro;
    }

    public void setPrediccionModelo(PrediccionModelo v) {
        prediccionModelo = v;
    }

    public void setReintentoDeSesion(SesionConocimientoIa v) {
        reintentoDeSesion = v;
    }

    public void setInstrumento(InstrumentoConocimiento v) {
        instrumento = v;
    }
    public void setPlanDiario(PlanDiario v) { planDiario = v; }

    public void setConfiguracionVersion(String v) {
        configuracionVersion = v;
    }

    public void setModelVersionPredictivo(String v) {
        modelVersionPredictivo = v;
    }

    public void setSchemaVersion(String v) {
        schemaVersion = v;
    }

    public void setProveedorIa(String v) {
        proveedorIa = v;
    }

    public void setModeloGenerativo(String v) {
        modeloGenerativo = v;
    }

    public void setEstado(EstadoSesionConocimientoIa v) {
        estado = v;
    }

    public void setGeneradoEn(Instant v) {
        generadoEn = v;
    }

    public void setFechaEvaluacion(LocalDate v) { fechaEvaluacion = v; }
    public void setClienteId(Long v) { clienteId = v; }
    public void setObjetivoCliente(String v) { objetivoCliente = v; }
    public void setClasificacionPredictiva(String v) { clasificacionPredictiva = v; }
    public void setMetaKcal(BigDecimal v) { metaKcal = v; }
    public void setMetaProteinaG(BigDecimal v) { metaProteinaG = v; }
    public void setMetaCarbohidratosG(BigDecimal v) { metaCarbohidratosG = v; }
    public void setMetaGrasasG(BigDecimal v) { metaGrasasG = v; }
    public void setMetaAguaMl(BigDecimal v) { metaAguaMl = v; }
    public void setPuntajeObtenido(BigDecimal v) { puntajeObtenido = v; }
    public void setPuntajeMaximo(BigDecimal v) { puntajeMaximo = v; }
    public void setNivelResultado(String v) { nivelResultado = v; }
    public void setEstadoValidez(EstadoValidezMedicion v) { estadoValidez = v; }
    public void setMotivoInvalidez(String v) { motivoInvalidez = v; }

    public void prepararReintentoGeneracion() {
        estado = EstadoSesionConocimientoIa.IA_NO_DISPONIBLE;
        generadoEn = null;
        codigoErrorTecnico = null;
        httpStatusGemini = null;
        fallidoEn = null;
        etapaError = null;
        tipoExcepcionSeguro = null;
        estadoValidez = EstadoValidezMedicion.NO_DETERMINADA;
        motivoInvalidez = null;
    }

    public void marcarRespondida(Instant instante) {
        if (estado != EstadoSesionConocimientoIa.GENERADA || respondidaEn != null) {
            throw new IllegalStateException("La sesión no admite una nueva entrega");
        }
        if (instante == null) {
            throw new IllegalArgumentException("La fecha de respuesta es obligatoria");
        }
        respondidaEn = instante;
        estado = EstadoSesionConocimientoIa.RESPONDIDA;
    }

    public void setCodigoErrorTecnico(String v) {
        codigoErrorTecnico = v;
    }

    public void setHttpStatusGemini(Integer v) {
        httpStatusGemini = v;
    }

    public void setFallidoEn(Instant v) {
        fallidoEn = v;
    }

    public void setEtapaError(String v) {
        etapaError = v;
    }

    public void setTipoExcepcionSeguro(String v) {
        tipoExcepcionSeguro = v;
    }
}
