package com.backend.nutri_predic.datasetmodelov5.controller;

import com.backend.nutri_predic.datasetmodelov5.dto.CalidadDatasetModeloV5Response;
import com.backend.nutri_predic.datasetmodelov5.dto.EstadoPreparacionClienteV5Response;
import com.backend.nutri_predic.datasetmodelov5.dto.PendientesEntrenamientoV5Response;
import com.backend.nutri_predic.datasetmodelov5.dto.PreparacionDatasetModeloV5Response;
import com.backend.nutri_predic.datasetmodelov5.service.DatasetModeloV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.EstadoPreparacionClienteV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.PendientesEntrenamientoV5Service;
import com.backend.nutri_predic.datasetmodelov5.service.PreparacionDatasetModeloV5Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ml/dataset/v5")
public class DatasetModeloV5Controller {
    private final DatasetModeloV5Service service;
    private final PreparacionDatasetModeloV5Service preparacion;
    private final EstadoPreparacionClienteV5Service estadoCliente;
    private final PendientesEntrenamientoV5Service pendientes;

    public DatasetModeloV5Controller(
            DatasetModeloV5Service service,
            PreparacionDatasetModeloV5Service preparacion,
            EstadoPreparacionClienteV5Service estadoCliente,
            PendientesEntrenamientoV5Service pendientes) {
        this.service = service;
        this.preparacion = preparacion;
        this.estadoCliente = estadoCliente;
        this.pendientes = pendientes;
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportar() {
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=dataset-modelo-v5.csv")
                .body(service.csv().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/calidad")
    public CalidadDatasetModeloV5Response calidad() {
        return service.calidad();
    }

    @GetMapping("/preparacion")
    public PreparacionDatasetModeloV5Response preparacion() {
        return preparacion.auditar();
    }

    @GetMapping("/pendientes")
    public PendientesEntrenamientoV5Response pendientes(
            @org.springframework.web.bind.annotation.RequestParam LocalDate fechaCorte) {
        return pendientes.listar(fechaCorte);
    }

    @GetMapping("/clientes/{clienteId}/estado")
    public EstadoPreparacionClienteV5Response estado(
            @org.springframework.web.bind.annotation.PathVariable Long clienteId,
            @org.springframework.web.bind.annotation.RequestParam LocalDate fechaCorte) {
        return estadoCliente.estado(clienteId, fechaCorte);
    }
}
