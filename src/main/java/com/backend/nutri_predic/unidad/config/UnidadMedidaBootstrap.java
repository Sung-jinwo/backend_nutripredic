package com.backend.nutri_predic.unidad.config;

import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UnidadMedidaBootstrap implements ApplicationRunner {
    private final UnidadMedidaRepository repo;

    public UnidadMedidaBootstrap(UnidadMedidaRepository r) {
        repo = r;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments a) {
        Map.of(
                        "MG",
                        "Miligramo",
                        "G",
                        "Gramo",
                        "MCG",
                        "Microgramo",
                        "ML",
                        "Mililitro",
                        "L",
                        "Litro",
                        "CAPSULA",
                        "Cápsula",
                        "TABLETA",
                        "Tableta",
                        "SCOOP",
                        "Scoop",
                        "PORCION",
                        "Porción",
                        "UNIDAD",
                        "Unidad")
                .forEach(
                        (c, n) -> {
                            if (!repo.existsByCodigo(c)) repo.save(new UnidadMedida(c, n));
                        });
    }
}
