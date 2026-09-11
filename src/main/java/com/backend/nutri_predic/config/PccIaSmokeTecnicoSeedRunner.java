package com.backend.nutri_predic.config;

import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.common.enums.MomentoEvaluacion;
import com.backend.nutri_predic.common.enums.Rol;
import com.backend.nutri_predic.perfilhabitos.entity.ClasificacionPerfilHabitos;
import com.backend.nutri_predic.prediccionmodelo.entity.EstadoPrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;
import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Profile({"dev", "test"})
public class PccIaSmokeTecnicoSeedRunner {
    static final String EMAIL = "smoke-tecnico-pcc-ia@dev.nutripredic.local";
    static final String MODEL_VERSION = "smoke-tecnico-pcc-ia-v5";
    static final String MARCAS = "SMOKE_TECNICO_PCC_IA | NO_USAR_TESIS | NO_USAR_ENTRENAMIENTO";

    @Bean
    CommandLineRunner seedPccIaSmokeTecnico(
            UsuarioRepository usuarios,
            ClienteRepository clientes,
            PrediccionModeloRepository predicciones) {
        return args -> crearSiFalta(usuarios, clientes, predicciones);
    }

    @Transactional
    void crearSiFalta(
            UsuarioRepository usuarios,
            ClienteRepository clientes,
            PrediccionModeloRepository predicciones) {
        var usuario =
                usuarios.findByEmail(EMAIL)
                        .orElseGet(
                                () ->
                                        usuarios.save(
                                                new Usuario(
                                                        EMAIL,
                                                        "SMOKE-TECNICO-NO-LOGIN",
                                                        "SMOKE TECNICO PCC-IA - NO USAR TESIS",
                                                        Rol.CLIENTE)));
        var cliente =
                clientes.findByUsuarioId(usuario.getId())
                        .orElseGet(
                                () -> {
                                    var creado = new Cliente(usuario);
                                    creado.setObjetivoFisico(MARCAS);
                                    return clientes.save(creado);
                                });
        boolean existe =
                predicciones
                        .findByClienteIdAndEstadoOrderByFechaPrediccionDesc(
                                cliente.getId(), EstadoPrediccionModelo.EXITOSA)
                        .stream()
                        .anyMatch(p -> MODEL_VERSION.equals(p.getModelVersion()));
        if (existe) return;
        var prediccion = new PrediccionModelo();
        prediccion.setCliente(cliente);
        prediccion.setFechaCorte(LocalDate.now());
        prediccion.setMomentoEvaluacion(MomentoEvaluacion.NO_DETERMINADO);
        prediccion.setSchemaVersion("variables-modelo-v5");
        prediccion.setModelVersion(MODEL_VERSION);
        prediccion.setClasificacionPredicha(ClasificacionPerfilHabitos.MEJORABLE);
        prediccion.setProbAdecuado(new BigDecimal("0.200000"));
        prediccion.setProbMejorable(new BigDecimal("0.600000"));
        prediccion.setProbCritico(new BigDecimal("0.200000"));
        prediccion.setEstado(EstadoPrediccionModelo.EXITOSA);
        prediccion.setObservacionesTecnicas(MARCAS);
        predicciones.save(prediccion);
    }
}
