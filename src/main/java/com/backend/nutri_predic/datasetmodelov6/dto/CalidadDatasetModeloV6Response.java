package com.backend.nutri_predic.datasetmodelov6.dto;
import java.util.Map;
public record CalidadDatasetModeloV6Response(long totalEvaluaciones,long filasEntrenables,Map<String,Long> noEntrenablesPorMotivo,Map<String,Long> distribucionClasificacion){}
