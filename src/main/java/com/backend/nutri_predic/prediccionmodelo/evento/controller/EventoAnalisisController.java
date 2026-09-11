package com.backend.nutri_predic.prediccionmodelo.evento.controller;

import com.backend.nutri_predic.prediccionmodelo.evento.dto.*;
import com.backend.nutri_predic.prediccionmodelo.evento.service.EventoAnalisisService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analisis")
public class EventoAnalisisController {
    private final EventoAnalisisService service;

    public EventoAnalisisController(EventoAnalisisService s) {
        service = s;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoAnalisisResponse iniciar(
            @Valid @RequestBody InicioAnalisisRequest r, Authentication a) {
        return service.iniciar(r, a);
    }

    @PatchMapping("/{id}/finalizar")
    public EventoAnalisisResponse finalizar(
            @PathVariable Long id,
            @Valid @RequestBody FinalizarAnalisisRequest r,
            Authentication a) {
        return service.finalizar(id, r, a);
    }

    @GetMapping("/{id}")
    public EventoAnalisisResponse get(@PathVariable Long id, Authentication a) {
        return service.get(id, a);
    }
}
