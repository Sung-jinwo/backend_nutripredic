package com.backend.nutri_predic.alimentacion.nutricion.service;

import com.backend.nutri_predic.alimentacion.repository.RegistroAlimentoRepository;
import com.backend.nutri_predic.alimentacion.service.NutricionAlimentoService;
import com.backend.nutri_predic.common.exception.ResourceNotFoundException;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.alimentacion.nutricion.dto.*;
import com.backend.nutri_predic.suplemento.repository.RegistroConsumoSuplementoRepository;
import com.backend.nutri_predic.suplemento.service.NutricionSuplementoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumoNutricionalDiarioService {
    private final ClienteRepository clientes;
    private final RegistroAlimentoRepository alimentos;
    private final RegistroConsumoSuplementoRepository suplementos;
    private final NutricionAlimentoService nutricionAlimento;
    private final NutricionSuplementoService nutricionSuplemento;

    public ConsumoNutricionalDiarioService(
            ClienteRepository clientes, RegistroAlimentoRepository alimentos,
            RegistroConsumoSuplementoRepository suplementos, NutricionAlimentoService nutricionAlimento,
            NutricionSuplementoService nutricionSuplemento) {
        this.clientes = clientes;
        this.alimentos = alimentos;
        this.suplementos = suplementos;
        this.nutricionAlimento = nutricionAlimento;
        this.nutricionSuplemento = nutricionSuplemento;
    }

    @Transactional(readOnly = true)
    public ConsumoNutricionalDiarioResponse obtener(Long clienteId, LocalDate fecha) {
        if (!clientes.existsById(clienteId)) throw new ResourceNotFoundException("Cliente");
        var registrosAlimento = alimentos
                .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(clienteId, fecha, fecha);
        var registrosSuplemento = suplementos
                .findByRegistroHabitoClienteIdAndRegistroHabitoFechaBetweenOrderByRegistroHabitoFechaAscIdAsc(clienteId, fecha, fecha);
        var kcal = new Acumulado();
        var proteina = new Acumulado();
        var carbohidratos = new Acumulado();
        var grasas = new Acumulado();
        for (var registro : registrosAlimento) {
            var aporte = nutricionAlimento.calcular(registro);
            kcal.agregar(aporte.calculable() ? aporte.kcal() : null);
            proteina.agregar(aporte.calculable() ? aporte.proteinaG() : null);
            carbohidratos.agregar(aporte.calculable() ? aporte.carbohidratosG() : null);
            grasas.agregar(aporte.calculable() ? aporte.grasasG() : null);
        }
        for (var registro : registrosSuplemento) {
            var aporte = nutricionSuplemento.calcular(registro);
            kcal.agregar(nutricionSuplemento.calcularEnergiaKcal(registro));
            proteina.agregar(aporte.calculable() ? aporte.proteinaG() : null);
            carbohidratos.agregar(aporte.calculable() ? aporte.carbohidratosG() : null);
            grasas.agregar(aporte.calculable() ? aporte.grasasG() : null);
        }
        return new ConsumoNutricionalDiarioResponse(
                clienteId, fecha, kcal.resultado(), proteina.resultado(), carbohidratos.resultado(), grasas.resultado(),
                registrosAlimento.size(), registrosSuplemento.size());
    }

    private static final class Acumulado {
        private BigDecimal valor = BigDecimal.ZERO;
        private boolean tieneRegistros;
        private boolean completo = true;
        void agregar(BigDecimal aporte) {
            tieneRegistros = true;
            if (aporte == null) completo = false;
            else valor = valor.add(aporte);
        }
        NutrienteDiarioResponse resultado() {
            return new NutrienteDiarioResponse(tieneRegistros && completo ? valor : null, tieneRegistros && completo);
        }
    }
}
