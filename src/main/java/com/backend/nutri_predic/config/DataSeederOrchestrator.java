package com.backend.nutri_predic.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@ConditionalOnProperty(name = "app.demo-seed.enabled", havingValue = "true")
public class DataSeederOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(DataSeederOrchestrator.class);

    @Bean
    CommandLineRunner seedBaseData(AdminDemoDataSeeder seeder) {
        return args -> {
            var ayer = LocalDate.now(ZoneId.of("America/Lima")).minusDays(1);
            seeder.estado = "PREPARANDO";
            var clientes = new LinkedHashMap<AdminDemoDataSeeder.DemoCase, Long>();
            for (var caso : AdminDemoDataSeeder.CASES) {
                try { clientes.put(caso, seeder.seedInputs(caso, ayer)); }
                catch (Exception cause) { log.warn("Seed DEMO {}: no se guardaron las entradas ({})", caso.id(), cause.getClass().getSimpleName()); }
            }
            // External services must not block readiness. Exactly one finite pass, no polling/retry loop.
            Thread.ofVirtual().name("admin-demo-seed").start(() -> {
                for (var entry : clientes.entrySet()) {
                    try {
                        if (seeder.process(entry.getKey(), entry.getValue(), ayer)) seeder.completados++;
                        else log.warn("Seed DEMO {}: ciclo pendiente/fallido; revisar Análisis", entry.getKey().id());
                    } catch (Exception cause) {
                        log.warn("Seed DEMO {}: procesamiento incompleto ({})", entry.getKey().id(), cause.getClass().getSimpleName());
                    }
                }
                seeder.estado = seeder.completados == AdminDemoDataSeeder.CASES.size() ? "COMPLETADO" : "PARCIAL";
                log.info("Seed DEMO {}: {}/{} ciclos completos. NO usar como evidencia de tesis ni entrenamiento.",
                        seeder.estado, seeder.completados, AdminDemoDataSeeder.CASES.size());
            });
        };
    }
}
