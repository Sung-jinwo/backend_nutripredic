package com.backend.nutri_predic.datasetmodelov6.dto;
import java.time.LocalDate; import java.util.List;
public record PendientesEntrenamientoV6Response(LocalDate fechaCorte,int xTotal,long filasEntrenables,List<EstadoPreparacionClienteV6Response> candidatos){}
