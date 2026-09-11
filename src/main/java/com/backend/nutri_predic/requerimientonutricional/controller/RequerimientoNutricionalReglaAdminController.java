package com.backend.nutri_predic.requerimientonutricional.controller;

import com.backend.nutri_predic.requerimientonutricional.dto.RequerimientoNutricionalReglaRequest;
import com.backend.nutri_predic.requerimientonutricional.service.RequerimientoNutricionalService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/requerimientos-nutricionales/reglas")
@PreAuthorize("hasRole('ADMIN')")
public class RequerimientoNutricionalReglaAdminController {
    private final RequerimientoNutricionalService service;
    public RequerimientoNutricionalReglaAdminController(RequerimientoNutricionalService service) { this.service = service; }
    @PostMapping
    public Map<String, Long> crear(@Valid @RequestBody RequerimientoNutricionalReglaRequest request) {
        return Map.of("id", service.crearRegla(request));
    }
}
