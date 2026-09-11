package com.backend.nutri_predic.conocimiento.evaluacion.controller;

import com.backend.nutri_predic.conocimiento.dto.InstrumentoPublicoResponse;
import com.backend.nutri_predic.conocimiento.service.InstrumentoConocimientoService;
import com.backend.nutri_predic.conocimiento.evaluacion.dto.*;
import com.backend.nutri_predic.conocimiento.evaluacion.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {
    private final TestService service;
    private final InstrumentoConocimientoService instrumentos;

    public TestController(TestService s, InstrumentoConocimientoService i) {
        service = s;
        instrumentos = i;
    }

    @GetMapping("/api/tests/instrumento-activo")
    public InstrumentoPublicoResponse instrumentoActivo() {
        return instrumentos.activo();
    }

    @PostMapping("/api/tests/respuestas")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Corregir y guardar un test de conocimiento")
    public ResultadoTestResponse submit(
            @Valid @RequestBody ResponderTestRequest r, Authentication a) {
        return service.submit(r, a);
    }

    @GetMapping("/api/clientes/{clienteId}/tests")
    @Operation(summary = "Listar resultados de test de un cliente")
    public List<ResultadoTestResponse> list(@PathVariable Long clienteId, Authentication a) {
        return service.list(clienteId, a);
    }

    @GetMapping("/api/clientes/{clienteId}/tests/{id}")
    public ResultadoTestResponse get(
            @PathVariable Long clienteId, @PathVariable Long id, Authentication a) {
        return service.get(clienteId, id, a);
    }
}
