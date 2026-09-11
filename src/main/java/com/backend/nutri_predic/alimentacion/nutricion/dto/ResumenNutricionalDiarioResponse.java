package com.backend.nutri_predic.alimentacion.nutricion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumenNutricionalDiarioResponse(
        LocalDate fecha,
        ObjetivoResumen objetivo,
        ConsumidoResumen consumido,
        DiferenciaResumen diferencia,
        PorcentajeResumen porcentajeCumplimiento,
        AguaResumen agua,
        String estado,
        String motivo) {

    public record ObjetivoResumen(String estado, BigDecimal kcal, BigDecimal proteinaG,
                                  BigDecimal carbohidratosG, BigDecimal grasasG,
                                  String fuenteReferencia, String versionReferencia, String motivo) {}

    public record AlimentosNutrientes(BigDecimal kcal, BigDecimal proteinaG, BigDecimal carbohidratosG,
                                      BigDecimal grasasG, BigDecimal fibraG, BigDecimal azucarG, BigDecimal sodioMg, boolean calculable) {}
    public record SuplementosNutrientes(BigDecimal kcal, BigDecimal proteinaG, BigDecimal carbohidratosG,
                                        BigDecimal grasasG, BigDecimal creatinaG, BigDecimal cafeinaMg, boolean calculable) {}
    public record TotalNutrientes(BigDecimal kcal, BigDecimal proteinaG, BigDecimal carbohidratosG,
                                  BigDecimal grasasG, BigDecimal fibraG, BigDecimal azucarG, BigDecimal sodioMg, boolean calculable) {}

    public record ConsumidoResumen(AlimentosNutrientes alimentos, SuplementosNutrientes suplementos, TotalNutrientes total,
                                   int registrosAlimento, int registrosSuplemento) {}

    public record DiferenciaResumen(BigDecimal kcal, BigDecimal proteinaG, BigDecimal carbohidratosG, BigDecimal grasasG) {}
    public record PorcentajeResumen(BigDecimal kcal, BigDecimal proteina, BigDecimal carbohidratos, BigDecimal grasas) {}
    public record AguaResumen(Integer consumidoMl, BigDecimal objetivoMl, Double consumoLitrosOriginal,
                              boolean declarado, Boolean consumeSuplementosDeclarado) {}
}
