package com.backend.nutri_predic.unidad.controller;

import com.backend.nutri_predic.unidad.dto.UnidadMedidaResponse;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/unidades")
public class UnidadMedidaController {
    private final UnidadMedidaRepository unidades;

    public UnidadMedidaController(UnidadMedidaRepository u) {
        unidades = u;
    }

    @GetMapping
    public List<UnidadMedidaResponse> listar() {
        return unidades.findByActivaTrueOrderByCodigoAsc().stream()
                .map(UnidadMedidaResponse::from)
                .toList();
    }
}
