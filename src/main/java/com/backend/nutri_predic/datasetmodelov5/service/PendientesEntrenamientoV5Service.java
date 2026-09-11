package com.backend.nutri_predic.datasetmodelov5.service;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.datasetmodelov5.dto.EstadoPreparacionClienteV5Response;
import com.backend.nutri_predic.datasetmodelov5.dto.PendientesEntrenamientoV5Response;
import com.backend.nutri_predic.datasetmodelov5.dto.PendientesEntrenamientoV5Response.Candidato;
import com.backend.nutri_predic.datasetmodelov5.dto.PendientesEntrenamientoV5Response.DatosObservacionales;
import com.backend.nutri_predic.datasetmodelov5.dto.PendientesEntrenamientoV5Response.GroundTruth;
import com.backend.nutri_predic.datasetmodelov5.dto.PendientesEntrenamientoV5Response.Ventana;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.entity.EvaluacionPerfilHabitos;
import com.backend.nutri_predic.perfilhabitos.repository.EvaluacionPerfilHabitosRepository;
import com.backend.nutri_predic.variablemodelov5.schema.FeatureSchemaV5Mapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PendientesEntrenamientoV5Service {
    private static final int DIAS_VENTANA = 7;
    private static final String CRITERIO_PRIORIDAD =
            "GROUND_TRUTH_VALIDO_PRIMERO; X_DISPONIBLES_DESC; CLIENTE_ID_ASC";

    private final ClienteRepository clientes;
    private final EvaluacionPerfilHabitosRepository evaluaciones;
    private final EstadoPreparacionClienteV5Service estadoCliente;
    private final DatasetModeloV5Service dataset;
    private final AptitudEntrenamientoV5Service aptitud;

    public PendientesEntrenamientoV5Service(
            ClienteRepository clientes,
            EvaluacionPerfilHabitosRepository evaluaciones,
            EstadoPreparacionClienteV5Service estadoCliente,
            DatasetModeloV5Service dataset,
            AptitudEntrenamientoV5Service aptitud) {
        this.clientes = clientes;
        this.evaluaciones = evaluaciones;
        this.estadoCliente = estadoCliente;
        this.dataset = dataset;
        this.aptitud = aptitud;
    }

    @Transactional(readOnly = true)
    public PendientesEntrenamientoV5Response listar(LocalDate fechaCorte) {
        List<EvaluacionPerfilHabitos> todasEvaluaciones = evaluaciones.findAllByOrderByIdAsc();
        List<BorradorCandidato> todosBorradores =
                clientes.findAll().stream()
                        .map(cliente -> diagnosticar(cliente, fechaCorte, todasEvaluaciones))
                        .toList();
        List<BorradorCandidato> borradores =
                todosBorradores.stream()
                        .filter(borrador -> !borrador.smokeTecnico())
                        .filter(borrador -> !borrador.aptoParaEntrenamiento())
                        .sorted(ordenPrioridad())
                        .toList();
        List<Candidato> candidatos = new ArrayList<>();
        for (int indice = 0; indice < borradores.size(); indice++) {
            BorradorCandidato borrador = borradores.get(indice);
            candidatos.add(
                    new Candidato(
                            indice + 1,
                            borrador.clienteId(),
                            fechaCorte,
                            borrador.datosObservacionales(),
                            borrador.groundTruth(),
                            false,
                            false));
        }

        var filasTrainables = dataset.filas();
        Map<String, Long> distribucion = new LinkedHashMap<>();
        for (ClasificacionPerfilHabitos clasificacion : ClasificacionPerfilHabitos.values()) {
            distribucion.put(clasificacion.name(), 0L);
        }
        filasTrainables.forEach(
                fila ->
                        distribucion.computeIfPresent(
                                fila.clasificacionReal(), (clave, n) -> n + 1));
        boolean listo = distribucion.values().stream().allMatch(cantidad -> cantidad >= 3);

        return new PendientesEntrenamientoV5Response(
                fechaCorte,
                FeatureSchemaV5Mapper.FEATURE_NAMES.size(),
                CRITERIO_PRIORIDAD,
                new PendientesEntrenamientoV5Response.Resumen(
                        clientes.count(),
                        candidatos.size(),
                        todosBorradores.stream().filter(BorradorCandidato::smokeTecnico).count(),
                        todosBorradores.stream()
                                .filter(borrador -> !borrador.smokeTecnico())
                                .filter(BorradorCandidato::aptoParaEntrenamiento)
                                .count(),
                        filasTrainables.size(),
                        Map.copyOf(distribucion),
                        listo),
                List.copyOf(candidatos));
    }

    private BorradorCandidato diagnosticar(
            Cliente cliente,
            LocalDate fechaCorte,
            List<EvaluacionPerfilHabitos> todasEvaluaciones) {
        EstadoPreparacionClienteV5Response estado =
                estadoCliente.estado(cliente.getId(), fechaCorte);
        List<EvaluacionPerfilHabitos> evaluacionesCliente =
                todasEvaluaciones.stream()
                        .filter(
                                evaluacion ->
                                        evaluacion.getCliente().getId().equals(cliente.getId()))
                        .toList();
        List<EvaluacionPerfilHabitos> relacionadas =
                evaluacionesCliente.stream()
                        .filter(evaluacion -> fechaCorte.equals(evaluacion.getFechaCorte()))
                        .toList();
        boolean smoke =
                aptitud.esClienteTecnico(cliente)
                        || evaluacionesCliente.stream().anyMatch(aptitud::esSmokeTecnico);
        EvaluacionPerfilHabitos groundTruthSeleccionado = seleccionarGroundTruth(relacionadas);
        GroundTruth groundTruth = groundTruth(groundTruthSeleccionado);
        List<String> pendientesObservacionales =
                estado.pendientes().stream()
                        .filter(pendiente -> !"REGISTRAR_GROUND_TRUTH".equals(pendiente))
                        .toList();
        DatosObservacionales observacionales =
                new DatosObservacionales(
                        estado.perfilCompleto(),
                        perfilBaseFaltante(estado),
                        estado.motivosXNull().containsKey("tipo_objetivo_fisico"),
                        ventana(estado.alimentacionDiasCompletos()),
                        ventana(estado.suplementacionDiasCompletos()),
                        ventana(estado.habitosDiasCompletos()),
                        estado.xDisponibles(),
                        FeatureSchemaV5Mapper.FEATURE_NAMES.size(),
                        estado.motivosXNull().keySet().stream().sorted().toList(),
                        estado.motivosXNull(),
                        pendientesObservacionales);
        return new BorradorCandidato(
                cliente.getId(),
                observacionales,
                groundTruth,
                smoke,
                estado.aptoParaEntrenamiento());
    }

    private boolean perfilBaseFaltante(EstadoPreparacionClienteV5Response estado) {
        return List.of("edad", "peso_kg", "altura_cm").stream()
                .anyMatch(estado.motivosXNull()::containsKey);
    }

    private EvaluacionPerfilHabitos seleccionarGroundTruth(
            List<EvaluacionPerfilHabitos> evaluacionesRelacionadas) {
        return evaluacionesRelacionadas.stream()
                .filter(evaluacion -> !aptitud.esSmokeTecnico(evaluacion))
                .max(
                        Comparator.comparing(aptitud::groundTruthValido)
                                .thenComparing(
                                        EvaluacionPerfilHabitos::getId,
                                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElse(null);
    }

    private GroundTruth groundTruth(EvaluacionPerfilHabitos evaluacion) {
        if (evaluacion == null) {
            return new GroundTruth(false, null, null, null, null, null, null, null, false);
        }
        var rubrica = evaluacion.getRubrica();
        return new GroundTruth(
                true,
                evaluacion.getId(),
                evaluacion.getEstadoValidez(),
                aptitud.rubricaValida(evaluacion),
                rubrica == null ? null : rubrica.getEstado(),
                rubrica == null ? null : rubrica.getCodigo(),
                rubrica == null ? null : rubrica.getVersion(),
                evaluacion.getClasificacionReal(),
                aptitud.groundTruthValido(evaluacion));
    }

    private Ventana ventana(int diasCompletos) {
        return new Ventana(
                diasCompletos == DIAS_VENTANA,
                diasCompletos,
                Math.max(0, DIAS_VENTANA - diasCompletos));
    }

    private Comparator<BorradorCandidato> ordenPrioridad() {
        return Comparator.comparing(BorradorCandidato::groundTruthValido)
                .reversed()
                .thenComparing(Comparator.comparingInt(BorradorCandidato::xDisponibles).reversed())
                .thenComparing(BorradorCandidato::clienteId);
    }

    private record BorradorCandidato(
            Long clienteId,
            DatosObservacionales datosObservacionales,
            GroundTruth groundTruth,
            boolean smokeTecnico,
            boolean aptoParaEntrenamiento) {
        private boolean groundTruthValido() {
            return groundTruth.groundTruthValido();
        }

        private int xDisponibles() {
            return datosObservacionales.xDisponibles();
        }
    }
}
