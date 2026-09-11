package com.backend.nutri_predic.prediccionmodelo.dto; import java.time.LocalDate; import java.util.*;
public record PreparacionAnalisisPredictivoResponse(Long clienteId,LocalDate fechaCorte,boolean puedeAnalizar,int diasCompletos,int diasRequeridos,Map<String,DominioPreparacionAnalisisResponse> dominios,int xDisponibles,int xTotal,List<String> datosFaltantes) {}
