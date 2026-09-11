package com.backend.nutri_predic.alimentacion.service;

import com.backend.nutri_predic.alimentacion.dto.*;
import com.backend.nutri_predic.alimentacion.entity.RegistroAlimento;
import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.alimentacion.habito.repository.RegistroHabitoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgregacionNutricionalService {
    private static final int DIAS_VENTANA = 7;
    private final ClienteRepository clientes;
    private final HistorialPerfilClienteRepository perfiles;
    private final RegistroHabitoRepository habitos;
    private final RegistroAlimentoRepository alimentos;
    private final NutricionAlimentoService nutricion;

    public AgregacionNutricionalService(
            ClienteRepository clientes,
            HistorialPerfilClienteRepository perfiles,
            RegistroHabitoRepository habitos,
            RegistroAlimentoRepository alimentos,
            NutricionAlimentoService nutricion) {
        this.clientes = clientes;
        this.perfiles = perfiles;
        this.habitos = habitos;
        this.alimentos = alimentos;
        this.nutricion = nutricion;
    }

    @Transactional(readOnly = true)
    public ResumenAgregacionNutricionalResponse resumir(Long clienteId, LocalDate fechaCorte) {
        var cliente =
                clientes.findById(clienteId)
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente"));
        var desde = fechaCorte.minusDays(DIAS_VENTANA - 1L);
        var porDia = new HashMap<LocalDate, List<RegistroAlimento>>();
        for (var registro :
                alimentos
                        .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(
                                clienteId, desde, fechaCorte)) {
            porDia.computeIfAbsent(
                            registro.getRegistroHabito().getFecha(), key -> new ArrayList<>())
                    .add(registro);
        }
        var diarios = new ArrayList<ResumenNutricionalDiario>();
        for (int i = 0; i < DIAS_VENTANA; i++)
            diarios.add(dia(desde.plusDays(i), porDia.getOrDefault(desde.plusDays(i), List.of())));
        var perfil =
                perfiles.findFirstByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(
                                clienteId, fechaCorte)
                        .orElse(null);
        BigDecimal peso = perfil != null ? perfil.getPesoKg() : cliente.getPesoKg();
        String fuentePeso =
                perfil != null
                        ? "HISTORIAL_PERFIL"
                        : (peso == null ? "SIN_PESO" : "CLIENTE_ACTUAL_FALLBACK");
        return new ResumenAgregacionNutricionalResponse(
                List.copyOf(diarios),
                ventana(desde, fechaCorte, diarios, clienteId, peso, fuentePeso));
    }

    private ResumenNutricionalDiario dia(LocalDate fecha, List<RegistroAlimento> registros) {
        var kcal = new Acumulado();
        var proteina = new Acumulado();
        var carbohidratos = new Acumulado();
        var grasas = new Acumulado();
        var fibra = new Acumulado();
        var azucar = new Acumulado();
        var sodio = new Acumulado();
        int calculables = 0;
        for (var registro : registros) {
            var aporte = nutricion.calcular(registro);
            if (!aporte.calculable()) continue;
            calculables++;
            kcal.agregar(aporte.kcal());
            proteina.agregar(aporte.proteinaG());
            carbohidratos.agregar(aporte.carbohidratosG());
            grasas.agregar(aporte.grasasG());
            fibra.agregar(aporte.fibraG());
            azucar.agregar(aporte.azucarG());
            sodio.agregar(aporte.sodioMg());
        }
        int total = registros.size();
        return new ResumenNutricionalDiario(
                fecha,
                kcal.diario(total),
                proteina.diario(total),
                carbohidratos.diario(total),
                grasas.diario(total),
                fibra.diario(total),
                azucar.diario(total),
                sodio.diario(total),
                total,
                calculables,
                total - calculables,
                porcentaje(calculables, total));
    }

    private ResumenNutricionalVentana ventana(
            LocalDate desde,
            LocalDate hasta,
            List<ResumenNutricionalDiario> dias,
            Long clienteId,
            BigDecimal peso,
            String fuentePeso) {
        var kcal = new Acumulado();
        var proteina = new Acumulado();
        var carbohidratos = new Acumulado();
        var grasas = new Acumulado();
        var fibra = new Acumulado();
        var azucar = new Acumulado();
        var sodio = new Acumulado();
        int total = 0, calculables = 0, diasAlimentos = 0, diasNutricion = 0;
        for (var dia : dias) {
            total += dia.registrosAlimentoTotal();
            calculables += dia.registrosAlimentoCalculables();
            if (dia.registrosAlimentoTotal() > 0) diasAlimentos++;
            if (dia.registrosAlimentoCalculables() > 0) diasNutricion++;
            kcal.agregar(dia.kcal());
            proteina.agregar(dia.proteinaG());
            carbohidratos.agregar(dia.carbohidratosG());
            grasas.agregar(dia.grasasG());
            fibra.agregar(dia.fibraG());
            azucar.agregar(dia.azucarG());
            sodio.agregar(dia.sodioMg());
        }
        int diasHabitos =
                habitos.findByClienteIdAndFechaBetweenOrderByFechaAscIdAsc(clienteId, desde, hasta)
                        .size();
        var proteinaVentana = proteina.ventana(total, DIAS_VENTANA, diasNutricion);
        var kgVentana = dividir(proteinaVentana.promedioSobreVentanaConocido(), peso);
        var kgRegistrados = dividir(proteinaVentana.promedioSobreDiasConNutricionConocido(), peso);
        return new ResumenNutricionalVentana(
                desde,
                hasta,
                DIAS_VENTANA,
                diasHabitos,
                diasAlimentos,
                diasNutricion,
                total,
                calculables,
                total - calculables,
                porcentaje(calculables, total),
                kcal.ventana(total, DIAS_VENTANA, diasNutricion),
                proteinaVentana,
                carbohidratos.ventana(total, DIAS_VENTANA, diasNutricion),
                grasas.ventana(total, DIAS_VENTANA, diasNutricion),
                fibra.ventana(total, DIAS_VENTANA, diasNutricion),
                azucar.ventana(total, DIAS_VENTANA, diasNutricion),
                sodio.ventana(total, DIAS_VENTANA, diasNutricion),
                peso != null && peso.signum() > 0 ? peso : null,
                fuentePeso,
                kgVentana,
                kgRegistrados);
    }

    private static BigDecimal porcentaje(int parte, int total) {
        return total == 0
                ? null
                : BigDecimal.valueOf(parte)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal dividir(BigDecimal valor, BigDecimal divisor) {
        return valor == null || divisor == null || divisor.signum() <= 0
                ? null
                : valor.divide(divisor, 6, RoundingMode.HALF_UP);
    }

    private static class Acumulado {
        private BigDecimal valor;
        private int conocidos;
        private int totales;

        void agregar(BigDecimal v) {
            totales++;
            if (v != null) {
                valor = valor == null ? v : valor.add(v);
                conocidos++;
            }
        }

        void agregar(ResumenNutriente r) {
            totales += r.registrosTotales();
            conocidos += r.registrosConValor();
            if (r.valorConocido() != null)
                valor = valor == null ? r.valorConocido() : valor.add(r.valorConocido());
        }

        ResumenNutriente diario(int totalRegistros) {
            return totalRegistros == 0
                    ? new ResumenNutriente(null, null, null, 0, 0)
                    : new ResumenNutriente(
                            valor,
                            conocidos == totalRegistros,
                            porcentaje(conocidos, totalRegistros),
                            conocidos,
                            totalRegistros);
        }

        ResumenNutrienteVentana ventana(int totalRegistros, int diasVentana, int diasNutricion) {
            return totalRegistros == 0
                    ? new ResumenNutrienteVentana(null, null, null, 0, 0, null, null)
                    : new ResumenNutrienteVentana(
                            valor,
                            conocidos == totalRegistros,
                            porcentaje(conocidos, totalRegistros),
                            conocidos,
                            totalRegistros,
                            valor == null
                                    ? null
                                    : valor.divide(
                                            BigDecimal.valueOf(diasVentana),
                                            6,
                                            RoundingMode.HALF_UP),
                            valor == null || diasNutricion == 0
                                    ? null
                                    : valor.divide(
                                            BigDecimal.valueOf(diasNutricion),
                                            6,
                                            RoundingMode.HALF_UP));
        }
    }
}
