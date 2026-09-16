package com.backend.nutri_predic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminDemoStatusController {
    private final AdminDemoDataSeeder seeder;
    private final boolean enabled;
    public AdminDemoStatusController(AdminDemoDataSeeder seeder, @Value("${app.demo-seed.enabled:false}") boolean enabled) {
        this.seeder = seeder; this.enabled = enabled;
    }
    public record Status(boolean enabled, long demoUsers, String state, int completedCycles, String message) {}
    @GetMapping("/api/admin/demo-data/status")
    public Status status() {
        return new Status(enabled, seeder.seededUsers(), seeder.estado, seeder.completados,
                "Incluye clientes artificiales de demostración. No usar estos agregados como evidencia experimental ni entrenamiento.");
    }
}
