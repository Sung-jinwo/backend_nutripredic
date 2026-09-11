package com.backend.nutri_predic.variablemodelov5.dto;

import java.math.*;
import java.time.*;
import java.util.*;

public record VariablesModeloV5Response(
        String schemaVersion, LocalDate fechaCorte, Features features, Metadata metadata) {
    public record Features(
            Integer edad,
            BigDecimal pesoKg,
            BigDecimal alturaCm,
            String tipoObjetivoFisico,
            BigDecimal promedioKcal7d,
            BigDecimal promedioProteina7d,
            BigDecimal promedioCarbohidratos7d,
            BigDecimal promedioGrasas7d,
            BigDecimal promedioFibra7d,
            BigDecimal promedioAzucar7d,
            BigDecimal promedioSodio7d,
            BigDecimal promedioProteinaSuplementaria7d,
            BigDecimal promedioCreatina7d,
            BigDecimal promedioCafeina7d,
            BigDecimal promedioCarbohidratosSuplementarios7d,
            BigDecimal promedioGrasasSuplementarias7d,
            BigDecimal promedioCantidadComidas7d,
            BigDecimal promedioConsumoAgua7d,
            BigDecimal proporcionDesayuno7d,
            BigDecimal proporcionSnacks7d,
            BigDecimal promedioComidasCocinadas7d) {}

    public record Metadata(
            Long clienteId,
            java.util.UUID clienteGroupId,
            String fuentePerfil,
            boolean alimentacionCompleta,
            boolean suplementacionCompleta,
            boolean habitosCompletos) {}
}
