package com.backend.nutri_predic.datasetmodelov6.dto;
import java.util.List;
public record PreparacionDatasetModeloV6Response(long totalCortesAuditados,long filasEntrenables,List<EstadoPreparacionClienteV6Response> filas){}
