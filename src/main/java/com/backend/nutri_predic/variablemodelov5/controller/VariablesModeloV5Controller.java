package com.backend.nutri_predic.variablemodelov5.controller;

import com.backend.nutri_predic.common.service.AccessService;
import com.backend.nutri_predic.variablemodelov5.dto.*;
import com.backend.nutri_predic.variablemodelov5.service.*;
import java.time.*;
import org.springframework.security.core.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interno/variables-modelo-v5")
public class VariablesModeloV5Controller {
    private final VariablesModeloV5Service service;
    private final AccessService access;

    public VariablesModeloV5Controller(VariablesModeloV5Service s, AccessService a) {
        service = s;
        access = a;
    }

    @GetMapping("/clientes/{clienteId}")
    public VariablesModeloV5Response construir(
            @PathVariable Long clienteId, @RequestParam LocalDate fechaCorte, Authentication auth) {
        access.client(clienteId, auth);
        return service.construir(clienteId, fechaCorte);
    }
}
