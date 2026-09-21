package com.backend.nutri_predic.alimentacion.controller;

import com.backend.nutri_predic.alimentacion.dto.RegistroAlimentoResponse;
import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import com.backend.nutri_predic.alimentacion.service.AlimentacionService;
import com.backend.nutri_predic.alimentacion.service.FoodDataCentralService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class FoodDataCentralController {
    public record Consumir(@Positive long fdcId,
            @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("10000") BigDecimal gramos,
            @NotNull MomentoComida momentoComida) {}
    private final FoodDataCentralService foods;
    private final AlimentacionService registros;
    public FoodDataCentralController(FoodDataCentralService foods, AlimentacionService registros) {
        this.foods = foods; this.registros = registros;
    }
    @GetMapping("/api/alimentos/usda/buscar")
    public List<FoodDataCentralService.Food> buscar(@RequestParam String query) { return foods.buscar(query); }
    @PostMapping("/api/habitos/{habitoId}/alimentos/usda")
    public RegistroAlimentoResponse agregar(@PathVariable Long habitoId, @Valid @RequestBody Consumir r, Authentication auth) {
        registros.listarRegistro(habitoId, auth); // Verify ownership before spending provider quota.
        return registros.agregarRegistro(habitoId, foods.registro(r.fdcId(), r.gramos(), r.momentoComida()), auth);
    }
    @PutMapping("/api/habitos/{habitoId}/alimentos/{id}/usda")
    public RegistroAlimentoResponse editar(@PathVariable Long habitoId, @PathVariable Long id,
            @Valid @RequestBody Consumir r, Authentication auth) {
        registros.listarRegistro(habitoId, auth);
        return registros.actualizarRegistro(habitoId, id, foods.registro(r.fdcId(), r.gramos(), r.momentoComida()), auth);
    }
}
