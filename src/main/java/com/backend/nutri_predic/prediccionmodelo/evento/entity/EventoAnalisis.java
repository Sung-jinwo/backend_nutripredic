package com.backend.nutri_predic.prediccionmodelo.evento.entity;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.common.enums.EstadoValidezMedicion;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.estudio.entity.ParticipacionEstudio;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "eventos_analisis")
public class EventoAnalisis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version_evento")
    private Long versionEvento;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "procedimiento_analisis_id")
    private ProcedimientoAnalisis procedimiento;

    @ManyToOne
    @JoinColumn(name = "participacion_estudio_id")
    private ParticipacionEstudio participacionEstudio;

    @ManyToOne
    @JoinColumn(name = "evaluador_usuario_id")
    private Usuario evaluador;

    @ManyToOne
    @JoinColumn(name = "prediccion_modelo_id")
    private PrediccionModelo prediccionModelo;

    private LocalDate fechaCorte;
    private Instant datosListosEn;
    private Instant analisisIniciadoEn;
    private Instant variablesPreparadasEn;
    private Instant modeloSolicitadoEn;
    private Instant modeloRespondioEn;
    private Instant resultadoDisponibleEn;
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ciclo_diario", length = 20)
    private EstadoCicloDiario estadoCicloDiario;
    private Instant cicloCompletadoEn;
    private Long procesamientoCicloMs;
    @Column(length = 40) private String moduloFalloCiclo;
    @Column(length = 1000) private String motivoFalloCiclo;

    @Enumerated(EnumType.STRING)
    private MomentoEvaluacion momento = MomentoEvaluacion.NO_DETERMINADO;

    @Enumerated(EnumType.STRING)
    private EstadoValidezMedicion estadoValidez = EstadoValidezMedicion.NO_DETERMINADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen_resultado")
    private OrigenResultadoAnalisis origenResultado;

    private String motivoInvalidez;
    private String observacionesTecnicas;

    public void iniciarAnalisis(Instant inicio) {
        if (inicio == null || analisisIniciadoEn != null) {
            throw new IllegalStateException("El inicio del análisis sólo puede asignarse una vez");
        }
        analisisIniciadoEn = inicio;
    }

    public void prepararResultadoExitoso(
            PrediccionModelo prediccion, OrigenResultadoAnalisis origen) {
        validarSoftwareIa();
        validarPendiente();
        if (prediccion == null || origen == null) {
            throw new IllegalArgumentException(
                    "Una ejecución exitosa requiere predicción y origen");
        }
        prediccionModelo = prediccion;
        origenResultado = origen;
        estadoValidez = EstadoValidezMedicion.VALIDA;
        motivoInvalidez = null;
    }

    public void confirmarResultadoDisponible(Instant disponibleEn) {
        validarSoftwareIa();
        if (resultadoDisponibleEn != null) {
            throw new IllegalStateException("El análisis SOFTWARE_IA ya fue cerrado");
        }
        if (prediccionModelo == null
                || origenResultado == null
                || estadoValidez != EstadoValidezMedicion.VALIDA) {
            throw new IllegalStateException("El resultado predictivo aún no está persistido");
        }
        if (disponibleEn == null
                || analisisIniciadoEn == null
                || disponibleEn.isBefore(analisisIniciadoEn)) {
            throw new IllegalArgumentException("El cierre debe ser posterior al inicio");
        }
        resultadoDisponibleEn = disponibleEn;
    }

    public void marcarInvalido(
            String motivo, PrediccionModelo intentoFallido, OrigenResultadoAnalisis origen) {
        validarPendiente();
        prediccionModelo = intentoFallido;
        origenResultado = origen;
        estadoValidez = EstadoValidezMedicion.INVALIDA;
        motivoInvalidez = motivo;
    }

    public void cerrarManual(Instant disponibleEn, String observaciones) {
        if (procedimiento == null || procedimiento.getTipo() != TipoProcedimientoAnalisis.MANUAL) {
            throw new IllegalStateException("Sólo un análisis MANUAL admite cierre manual");
        }
        validarPendiente();
        if (disponibleEn == null
                || analisisIniciadoEn == null
                || disponibleEn.isBefore(analisisIniciadoEn)) {
            throw new IllegalArgumentException("El cierre debe ser posterior al inicio");
        }
        resultadoDisponibleEn = disponibleEn;
        observacionesTecnicas = observaciones;
        estadoValidez = EstadoValidezMedicion.VALIDA;
    }

    public void registrarVariablesPreparadas(Instant instante) {
        variablesPreparadasEn =
                asignarUnaVez(variablesPreparadasEn, instante, "variablesPreparadasEn");
    }

    public void registrarModeloSolicitado(Instant instante) {
        modeloSolicitadoEn = asignarUnaVez(modeloSolicitadoEn, instante, "modeloSolicitadoEn");
    }

    public void registrarModeloRespondio(Instant instante) {
        modeloRespondioEn = asignarUnaVez(modeloRespondioEn, instante, "modeloRespondioEn");
    }

    public void iniciarCicloDiario() {
        if (momento == MomentoEvaluacion.DIARIO && estadoCicloDiario == null) {
            estadoCicloDiario = EstadoCicloDiario.PENDIENTE;
            procesamientoCicloMs = 0L;
        }
    }

    public void registrarIntentoCiclo(
            long duracionActivaMs, boolean completo, String moduloFallo, String motivoFallo,
            Instant completadoEn) {
        if (momento != MomentoEvaluacion.DIARIO) return;
        if (estadoCicloDiario == EstadoCicloDiario.COMPLETADO) return;
        if (duracionActivaMs < 0) throw new IllegalArgumentException("La duración activa no puede ser negativa");
        procesamientoCicloMs = (procesamientoCicloMs == null ? 0L : procesamientoCicloMs) + duracionActivaMs;
        if (completo) {
            if (completadoEn == null) throw new IllegalArgumentException("El cierre del ciclo es obligatorio");
            estadoCicloDiario = EstadoCicloDiario.COMPLETADO;
            cicloCompletadoEn = completadoEn;
            moduloFalloCiclo = null;
            motivoFalloCiclo = null;
        } else {
            estadoCicloDiario = EstadoCicloDiario.FALLIDO;
            moduloFalloCiclo = moduloFallo;
            motivoFalloCiclo = motivoFallo;
        }
    }

    private Instant asignarUnaVez(Instant actual, Instant nuevo, String campo) {
        if (actual != null || nuevo == null) {
            throw new IllegalStateException(campo + " sólo puede asignarse una vez");
        }
        return nuevo;
    }

    private void validarSoftwareIa() {
        if (procedimiento == null
                || procedimiento.getTipo() != TipoProcedimientoAnalisis.SOFTWARE_IA) {
            throw new IllegalStateException("El evento no corresponde a SOFTWARE_IA");
        }
    }

    private void validarPendiente() {
        if (resultadoDisponibleEn != null
                || estadoValidez != EstadoValidezMedicion.NO_DETERMINADA) {
            throw new IllegalStateException("El análisis ya fue cerrado");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getVersionEvento() {
        return versionEvento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public ProcedimientoAnalisis getProcedimiento() {
        return procedimiento;
    }

    public ParticipacionEstudio getParticipacionEstudio() {
        return participacionEstudio;
    }

    public Usuario getEvaluador() {
        return evaluador;
    }

    public PrediccionModelo getPrediccionModelo() {
        return prediccionModelo;
    }

    public LocalDate getFechaCorte() {
        return fechaCorte;
    }

    public Instant getDatosListosEn() {
        return datosListosEn;
    }

    public Instant getAnalisisIniciadoEn() {
        return analisisIniciadoEn;
    }

    public Instant getVariablesPreparadasEn() {
        return variablesPreparadasEn;
    }

    public Instant getModeloSolicitadoEn() {
        return modeloSolicitadoEn;
    }

    public Instant getModeloRespondioEn() {
        return modeloRespondioEn;
    }

    public Instant getResultadoDisponibleEn() {
        return resultadoDisponibleEn;
    }
    public EstadoCicloDiario getEstadoCicloDiario() { return estadoCicloDiario; }
    public Instant getCicloCompletadoEn() { return cicloCompletadoEn; }
    public Long getProcesamientoCicloMs() { return procesamientoCicloMs; }
    public String getModuloFalloCiclo() { return moduloFalloCiclo; }
    public String getMotivoFalloCiclo() { return motivoFalloCiclo; }

    public MomentoEvaluacion getMomento() {
        return momento;
    }

    public EstadoValidezMedicion getEstadoValidez() {
        return estadoValidez;
    }

    public OrigenResultadoAnalisis getOrigenResultado() {
        return origenResultado;
    }

    public String getMotivoInvalidez() {
        return motivoInvalidez;
    }

    public String getObservacionesTecnicas() {
        return observacionesTecnicas;
    }

    public void setCliente(Cliente valor) {
        cliente = valor;
    }

    public void setProcedimiento(ProcedimientoAnalisis valor) {
        procedimiento = valor;
    }

    public void setParticipacionEstudio(ParticipacionEstudio valor) {
        participacionEstudio = valor;
    }

    public void setEvaluador(Usuario valor) {
        evaluador = valor;
    }

    public void setFechaCorte(LocalDate valor) {
        fechaCorte = valor;
    }

    public void setDatosListosEn(Instant valor) {
        datosListosEn = valor;
    }

    public void setMomento(MomentoEvaluacion valor) {
        momento = valor;
    }
}
