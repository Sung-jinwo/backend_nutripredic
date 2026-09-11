package com.backend.nutri_predic.dashboard.service;

import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.dashboard.dto.DashboardResponse;
import com.backend.nutri_predic.dashboard.dto.DashboardResponse.ModeloActivoResponse;
import com.backend.nutri_predic.indicador.service.PccIndicatorService;
import com.backend.nutri_predic.indicador.service.PcsIndicatorService;
import com.backend.nutri_predic.indicador.service.TppIndicatorService;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final ClienteRepository clientes;
    private final PrediccionModeloRepository predicciones;
    private final PccIndicatorService pcc;
    private final PcsIndicatorService pcs;
    private final TppIndicatorService tpp;

    public DashboardService(
            ClienteRepository clientes,
            PrediccionModeloRepository predicciones,
            PccIndicatorService pcc,
            PcsIndicatorService pcs,
            TppIndicatorService tpp) {
        this.clientes = clientes;
        this.predicciones = predicciones;
        this.pcc = pcc;
        this.pcs = pcs;
        this.tpp = tpp;
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        var indicadorPcc = pcc.obtener();
        var indicadorPcs = pcs.obtener();
        var indicadorTpp = tpp.obtener();
        // El nivel manual operativo se eliminó de Cliente: la clave se conserva
        // en null por contrato y el PCS oficial viene de pcs().
        // El modelo activo se deriva de la última predicción (modelVersion string).
        ModeloActivoResponse modeloActivo =
                predicciones
                        .findFirstByOrderByFechaPrediccionDesc()
                        .map(m -> new ModeloActivoResponse(m.getModelVersion(), m.getSchemaVersion()))
                        .orElse(null);
        return new DashboardResponse(
                clientes.count(),
                predicciones.count(),
                indicadorPcc,
                indicadorPcs,
                indicadorTpp,
                null,
                modeloActivo);
    }
}
