package com.backend.nutri_predic.config;

import com.backend.nutri_predic.alimentacion.dto.RegistroAlimentoRequest;
import com.backend.nutri_predic.alimentacion.entity.MomentoComida;
import com.backend.nutri_predic.alimentacion.habito.entity.RegistroHabito;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import com.backend.nutri_predic.alimentacion.service.AlimentacionService;
import com.backend.nutri_predic.cliente.entity.Cliente;
import com.backend.nutri_predic.cliente.entity.HistorialPerfilCliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.common.enums.*;
import com.backend.nutri_predic.conocimiento.practica.dto.ResponderConocimientoIaRequest;
import com.backend.nutri_predic.conocimiento.practica.entity.EstadoSesionConocimientoIa;
import com.backend.nutri_predic.conocimiento.practica.repository.PreguntaGeneradaIaRepository;
import com.backend.nutri_predic.conocimiento.practica.repository.SesionConocimientoIaRepository;
import com.backend.nutri_predic.conocimiento.practica.service.RespuestaConocimientoIaService;
import com.backend.nutri_predic.plandia.service.PlanDiarioService;
import com.backend.nutri_predic.prediccionmodelo.ciclo.service.CicloDiarioService;
import com.backend.nutri_predic.suplemento.dto.RegistroConsumoSuplementoRequest;
import com.backend.nutri_predic.suplemento.entity.SuplementoCatalogo;
import com.backend.nutri_predic.suplemento.entity.SuplementoCliente;
import com.backend.nutri_predic.suplemento.repository.SuplementoCatalogoRepository;
import com.backend.nutri_predic.suplemento.repository.SuplementoClienteRepository;
import com.backend.nutri_predic.suplemento.service.ConsumoSuplementoService;
import com.backend.nutri_predic.unidad.entity.UnidadMedida;
import com.backend.nutri_predic.unidad.repository.UnidadMedidaRepository;
import com.backend.nutri_predic.usuario.entity.Usuario;
import com.backend.nutri_predic.usuario.repository.UsuarioRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Artificial INPUTS for dashboard demonstration, never fabricated ML outputs. */
@Service
public class AdminDemoDataSeeder {
    public record DemoCase(int id, int proteinG, int carbsG, int fatG,
                           double waterL, int caffeineMg, int correctAnswers) {
        public String email() { return "demo-admin-v1-" + id + "@e2e.nutripredic.local"; }
    }
    public static final List<DemoCase> CASES = List.of(
            new DemoCase(1, 90, 240, 65, 1, 450, 0),
            new DemoCase(2, 155, 390, 104, 3, 100, 4),
            new DemoCase(3, 180, 480, 150, 2, 500, 2),
            new DemoCase(4, 135, 330, 90, 2.5, 0, 5));
    private static final String MARKER = "GENERATED_E2E_INPUTS NO_USAR_ENTRENAMIENTO";
    private final UsernamePasswordAuthenticationToken admin = new UsernamePasswordAuthenticationToken(
            "backend-demo-seeder", "", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    @Autowired UsuarioRepository usuarios;
    @Autowired ClienteRepository clientes;
    @Autowired HistorialPerfilClienteRepository perfiles;
    @Autowired RegistroHabitoRepository habitos;
    @Autowired UnidadMedidaRepository unidades;
    @Autowired SuplementoCatalogoRepository catalogo;
    @Autowired SuplementoClienteRepository suplementos;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AlimentacionService alimentos;
    @Autowired ConsumoSuplementoService consumos;
    @Autowired PlanDiarioService planes;
    @Autowired CicloDiarioService ciclos;
    @Autowired SesionConocimientoIaRepository sesiones;
    @Autowired PreguntaGeneradaIaRepository preguntas;
    @Autowired RespuestaConocimientoIaService respuestas;
    @Value("${app.demo-seed.password:}") String demoPassword;
    public volatile String estado = "NO_EJECUTADO";
    public volatile int completados;

    /** A whole input case is atomic. Existing users and daily records are never overwritten. */
    @Transactional
    public Long seedInputs(DemoCase caso, LocalDate ayer) {
        var usuario = usuarios.findByEmail(caso.email()).orElseGet(() -> {
            var password = demoPassword == null || demoPassword.isBlank() ? UUID.randomUUID().toString() : demoPassword;
            if (password.length() < 12) throw new IllegalArgumentException("DEMO_SEED_PASSWORD requiere al menos 12 caracteres");
            return usuarios.save(new Usuario(caso.email(), passwordEncoder.encode(password),
                    "DEMO ADMIN " + caso.id() + " — DATOS DE PRUEBA E2E ML", Rol.CLIENTE));
        });
        var cliente = clientes.findByUsuarioId(usuario.getId()).orElseGet(() -> {
            var c = new Cliente(usuario);
            c.setEdad(30); c.setSexo(SexoBiologico.MASCULINO);
            c.setPesoKg(BigDecimal.valueOf(80)); c.setAlturaCm(BigDecimal.valueOf(180));
            c.setObjetivoFisico("Mantener peso NO_USAR_ENTRENAMIENTO");
            c.setTipoObjetivoFisico(TipoObjetivoFisico.MANTENER_PESO);
            c.setObjetivoEnergetico(ObjetivoEnergetico.MANTENIMIENTO);
            c.setRealizaActividadFisica(true); c.setDiasEntrenamientoSemana(4);
            c.setTipoActividadFisica("Fuerza y caminata"); c.setTipoEntrenamiento(TipoEntrenamiento.FUERZA);
            c.setDuracionPromedioSesionMinutos(60);
            c = clientes.save(c);
            perfiles.save(new HistorialPerfilCliente(c, ayer));
            return c;
        });
        if (habitos.existsByClienteIdAndFecha(cliente.getId(), ayer)) return cliente.getId();
        var unidad = unidades.findByCodigoIgnoreCaseAndActivaTrue("G")
                .orElseGet(() -> unidades.save(new UnidadMedida("G", "Gramo")));
        unidades.findByCodigoIgnoreCaseAndActivaTrue("PORCION")
                .orElseGet(() -> unidades.save(new UnidadMedida("PORCION", "Porción")));
        var producto = catalogo.findByNombreIgnoreCase("DEMO NO CONSUMIR " + caso.id()).orElseGet(() -> {
            var s = new SuplementoCatalogo(); s.setNombre("DEMO NO CONSUMIR " + caso.id());
            s.setTipo("DEMO"); s.setActivo(true); return catalogo.save(s);
        });
        var suplemento = suplementos.findByClienteIdAndSuplementoId(cliente.getId(), producto.getId()).orElseGet(() -> {
            var s = new SuplementoCliente(); s.setCliente(cliente); s.setSuplemento(producto);
            s.setNombreDeclarado(producto.getNombre()); s.setCantidad(5d); s.setUnidad("G");
            s.setCantidadPorToma(BigDecimal.valueOf(5)); s.setUnidadMedida(unidad);
            s.setFechaInicio(ayer); s.setActivo(true); s.setComponentesDeclarados(MARKER + " / NO CONSUMIR");
            s.setProteinaGPorToma(BigDecimal.ZERO); s.setCarbohidratosGPorToma(BigDecimal.ZERO);
            s.setGrasasGPorToma(BigDecimal.ZERO); s.setCreatinaGPorToma(BigDecimal.ZERO);
            s.setCafeinaMgPorToma(BigDecimal.valueOf(caso.caffeineMg())); s.setSodioMgPorToma(BigDecimal.ZERO);
            return suplementos.save(s);
        });
        var h = new RegistroHabito(); h.setCliente(cliente); h.setFecha(ayer);
        h.setConsumoAgua(caso.waterL()); h.setCantidadComidas(1); h.setConsumeSuplementos(true);
        h.setAlimentos(MARKER); h = habitos.save(h);
        alimentos.agregarRegistro(h.getId(), new RegistroAlimentoRequest(null, "DEMO comida " + caso.id(),
                BigDecimal.ONE, "PORCION", MomentoComida.ALMUERZO, BigDecimal.valueOf(caso.proteinG()),
                BigDecimal.valueOf(caso.carbsG()), BigDecimal.valueOf(caso.fatG())), admin);
        consumos.crear(h.getId(), new RegistroConsumoSuplementoRequest(suplemento.getId(),
                BigDecimal.valueOf(5), "G", 1, MARKER + " / NO CONSUMIR"), admin);
        return cliente.getId();
    }

    /** Calls real AI/Gemini services. Failed cycles remain failed; times/probabilities are not inserted. */
    public boolean process(DemoCase caso, Long clienteId, LocalDate ayer) {
        planes.generarInicial(clienteId, ayer);
        var ciclo = ciclos.asegurar(clienteId, admin);
        if (!"COMPLETADO".equals(ciclo.estado())) return false;
        var sesion = sesiones.findByPrediccionModeloIdAndConfiguracionVersion(ciclo.prediccionId(), "pcc-ia-v1")
                .orElseThrow();
        if (sesion.getEstado() == EstadoSesionConocimientoIa.RESPONDIDA) return true;
        var cuestiones = preguntas.findBySesionIdOrderByOrdenAsc(sesion.getId());
        if (cuestiones.size() != 5) return false;
        var entregadas = new ArrayList<ResponderConocimientoIaRequest.Respuesta>();
        for (int i = 0; i < cuestiones.size(); i++) {
            var q = cuestiones.get(i);
            String correcta = q.getRespuestaCorrecta();
            String elegida = i < caso.correctAnswers() ? correcta : ("A".equalsIgnoreCase(correcta) ? "B" : "A");
            entregadas.add(new ResponderConocimientoIaRequest.Respuesta(q.getId(), elegida));
        }
        respuestas.responder(clienteId, sesion.getId(), new ResponderConocimientoIaRequest(entregadas), admin);
        return true;
    }

    @Transactional(readOnly = true)
    public long seededUsers() { return CASES.stream().filter(c -> usuarios.existsByEmail(c.email())).count(); }
}
