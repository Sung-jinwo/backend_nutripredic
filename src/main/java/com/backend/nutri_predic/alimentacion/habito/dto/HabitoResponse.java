package com.backend.nutri_predic.alimentacion.habito.dto;

import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import java.time.LocalDate;

public record HabitoResponse(
        Long id,
        Long clienteId,
        LocalDate fecha,
        Integer cantidadComidas,
        Double consumoAgua,
        Double proteinas,
        String tipoAlimentacion,
        String nivelOrganizacion,
        Boolean desayuno,
        Boolean snacks,
        String alimentos,
        Integer comidasCocinadas,
        String restricciones,
        Boolean consumeSuplementos) {
    public static HabitoResponse from(RegistroHabito h) {
        return new HabitoResponse(
                h.getId(),
                h.getCliente().getId(),
                h.getFecha(),
                h.getCantidadComidas(),
                h.getConsumoAgua(),
                h.getProteinas(),
                h.getTipoAlimentacion(),
                h.getNivelOrganizacion(),
                h.getDesayuno(),
                h.getSnacks(),
                h.getAlimentos(),
                h.getComidasCocinadas(),
                h.getRestricciones(),
                h.getConsumeSuplementos());
    }
}
