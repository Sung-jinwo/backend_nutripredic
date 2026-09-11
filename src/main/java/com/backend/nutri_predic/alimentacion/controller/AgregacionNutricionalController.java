package com.backend.nutri_predic.alimentacion.controller;

import com.backend.nutri_predic.alimentacion.dto.ResumenAgregacionNutricionalResponse;
import com.backend.nutri_predic.alimentacion.service.AgregacionNutricionalService;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/clientes/{clienteId}/nutricion")
@PreAuthorize("hasRole('ADMIN')")
public class AgregacionNutricionalController {
    private final AgregacionNutricionalService agregacion;

    public AgregacionNutricionalController(AgregacionNutricionalService agregacion) {
        this.agregacion = agregacion;
    }

    @GetMapping("/resumen")
    public ResumenAgregacionNutricionalResponse resumen(
            @PathVariable Long clienteId, @RequestParam LocalDate fechaCorte) {
        return agregacion.resumir(clienteId, fechaCorte);
    }
}
