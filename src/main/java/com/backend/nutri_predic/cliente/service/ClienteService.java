package com.backend.nutri_predic.cliente.service;

import com.backend.nutri_predic.cliente.dto.ClienteRequest;
import com.backend.nutri_predic.cliente.dto.ClienteResponse;
import com.backend.nutri_predic.cliente.dto.HistorialPerfilResponse;
import com.backend.nutri_predic.cliente.entity.HistorialPerfilCliente;
import com.backend.nutri_predic.cliente.repository.ClienteRepository;
import com.backend.nutri_predic.cliente.repository.HistorialPerfilClienteRepository;
import com.backend.nutri_predic.common.service.AccessService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final AccessService accessService;
    private final HistorialPerfilClienteRepository historial;

    public ClienteService(
            ClienteRepository clientes,
            AccessService accessService,
            HistorialPerfilClienteRepository historial) {
        this.clientes = clientes;
        this.accessService = accessService;
        this.historial = historial;
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> list() {
        return clientes.findAll().stream().map(ClienteResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse get(Long id, Authentication auth) {
        return ClienteResponse.from(accessService.client(id, auth));
    }

    @Transactional
    public ClienteResponse update(Long id, ClienteRequest request, Authentication auth) {
        var cliente = accessService.client(id, auth);
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && request.estado() != null)
            throw new com.backend.nutri_predic.common.exception.BusinessException(
                    "El cliente no puede modificar estado");
        var edadAnterior = cliente.getEdad();
        var sexoAnterior = cliente.getSexo();
        var pesoAnterior = cliente.getPesoKg();
        var alturaAnterior = cliente.getAlturaCm();
        var objetivoAnterior = cliente.getObjetivoFisico();
        var tipoObjetivoAnterior = cliente.getTipoObjetivoFisico();
        var actividadAnterior = cliente.getRealizaActividadFisica();
        var diasEntrenamientoAnterior = cliente.getDiasEntrenamientoSemana();
        var tipoActividadAnterior = cliente.getTipoActividadFisica();
        var tipoEntrenamientoAnterior = cliente.getTipoEntrenamiento();
        var duracionAnterior = cliente.getDuracionPromedioSesionMinutos();
        var objetivoEnergeticoAnterior = cliente.getObjetivoEnergetico();
        if (request.edad() != null) cliente.setEdad(request.edad());
        if (request.sexo() != null) cliente.setSexo(request.sexo());
        if (request.pesoKg() != null) {
            if (!admin && cliente.getPesoKg() != null
                    && !Objects.equals(cliente.getPesoKg(), request.pesoKg())) {
                throw new com.backend.nutri_predic.common.exception.BusinessException(
                        "El peso vigente se actualiza mediante el registro semanal");
            }
            cliente.setPesoKg(request.pesoKg());
        }
        if (request.alturaCm() != null) cliente.setAlturaCm(request.alturaCm());
        if (request.objetivoFisico() != null)
            cliente.setObjetivoFisico(request.objetivoFisico().trim());
        if (request.tipoObjetivoFisico() != null)
            cliente.setTipoObjetivoFisico(request.tipoObjetivoFisico());
        if (request.realizaActividadFisica() != null && !request.realizaActividadFisica()) {
            // Sin actividad: los campos de entrenamiento deben quedar nulos.
            cliente.setRealizaActividadFisica(false);
            cliente.setDiasEntrenamientoSemana(null);
            cliente.setTipoActividadFisica(null);
            cliente.setTipoEntrenamiento(null);
            cliente.setDuracionPromedioSesionMinutos(null);
        } else {
            if (request.realizaActividadFisica() != null)
                cliente.setRealizaActividadFisica(request.realizaActividadFisica());
            if (request.diasEntrenamientoSemana() != null)
                cliente.setDiasEntrenamientoSemana(request.diasEntrenamientoSemana());
            if (request.tipoActividadFisica() != null)
                cliente.setTipoActividadFisica(request.tipoActividadFisica().trim());
            if (request.tipoEntrenamiento() != null)
                cliente.setTipoEntrenamiento(request.tipoEntrenamiento());
            if (request.duracionPromedioSesionMinutos() != null)
                cliente.setDuracionPromedioSesionMinutos(request.duracionPromedioSesionMinutos());
            if (Boolean.TRUE.equals(cliente.getRealizaActividadFisica()))
                validarActividad(cliente);
        }
        // Flujo nuevo: objetivoEnergetico se deriva de tipoObjetivoFisico, no se acepta libre de cliente.
        // Solo ADMIN o compatibilidad histórica puede fijarlo explícitamente.
        if (request.objetivoEnergetico() != null) {
            if (admin) cliente.setObjetivoEnergetico(request.objetivoEnergetico());
            else {
                // Derivar desde tipoObjetivoFisico si está presente en request o ya en cliente
                var tipoParaDerivar = request.tipoObjetivoFisico() != null ? request.tipoObjetivoFisico() : cliente.getTipoObjetivoFisico();
                var derivado = derivarObjetivoEnergetico(tipoParaDerivar);
                if (derivado != null) cliente.setObjetivoEnergetico(derivado);
                // si no hay derivación (OTRO/MEJORAR sin regla) se deja sin setear para NO_DETERMINADA
            }
        } else if (request.tipoObjetivoFisico() != null) {
            // Auto-derivar cuando cliente actualiza objetivoFisico sin enviar energetico
            var derivado = derivarObjetivoEnergetico(request.tipoObjetivoFisico());
            if (derivado != null) cliente.setObjetivoEnergetico(derivado);
            else if (admin) cliente.setObjetivoEnergetico(null);
        }
        if (admin && request.estado() != null) cliente.setEstado(request.estado());
        cliente = clientes.save(cliente);
        boolean cambio =
                !Objects.equals(edadAnterior, cliente.getEdad())
                        || !Objects.equals(sexoAnterior, cliente.getSexo())
                        || !Objects.equals(pesoAnterior, cliente.getPesoKg())
                        || !Objects.equals(alturaAnterior, cliente.getAlturaCm())
                        || !Objects.equals(objetivoAnterior, cliente.getObjetivoFisico())
                        || !Objects.equals(tipoObjetivoAnterior, cliente.getTipoObjetivoFisico())
                        || !Objects.equals(actividadAnterior, cliente.getRealizaActividadFisica())
                        || !Objects.equals(diasEntrenamientoAnterior, cliente.getDiasEntrenamientoSemana())
                        || !Objects.equals(tipoActividadAnterior, cliente.getTipoActividadFisica())
                        || !Objects.equals(tipoEntrenamientoAnterior, cliente.getTipoEntrenamiento())
                        || !Objects.equals(duracionAnterior, cliente.getDuracionPromedioSesionMinutos())
                        || !Objects.equals(objetivoEnergeticoAnterior, cliente.getObjetivoEnergetico());
        if (cambio) {
            var hoy = LocalDate.now(ZoneId.of("America/Lima"));
            // Cierra la vigencia de snapshots abiertos y abre uno nuevo: el perfil
            // aplicable a fechaCorte=X se resuelve por rango [fechaDesde, vigenteHasta].
            historial.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(id).stream()
                    .filter(h -> h.getVigenteHasta() == null)
                    .forEach(
                            h -> {
                                h.setVigenteHasta(hoy);
                                historial.save(h);
                            });
            historial.save(new HistorialPerfilCliente(cliente, hoy));
        }
        return ClienteResponse.from(cliente);
    }

    private void validarActividad(com.backend.nutri_predic.cliente.entity.Cliente cliente) {
        var dias = cliente.getDiasEntrenamientoSemana();
        if (dias != null && (dias < 1 || dias > 7))
            throw new com.backend.nutri_predic.common.exception.BusinessException(
                    "diasEntrenamientoSemana debe estar entre 1 y 7 cuando realizaActividadFisica es true");
        if (dias != null
                && (cliente.getTipoActividadFisica() == null
                        || cliente.getTipoActividadFisica().isBlank()))
            throw new com.backend.nutri_predic.common.exception.BusinessException(
                    "tipoActividadFisica es requerido cuando se registra actividad física");
    }

    private com.backend.nutri_predic.common.enums.ObjetivoEnergetico derivarObjetivoEnergetico(com.backend.nutri_predic.common.enums.TipoObjetivoFisico tipo){
        if(tipo==null) return null;
        return switch(tipo){
            case PERDER_PESO -> com.backend.nutri_predic.common.enums.ObjetivoEnergetico.DEFICIT;
            case GANAR_PESO, GANAR_MASA_MUSCULAR -> com.backend.nutri_predic.common.enums.ObjetivoEnergetico.SUPERAVIT;
            case MANTENER_PESO -> com.backend.nutri_predic.common.enums.ObjetivoEnergetico.MANTENIMIENTO;
            case MEJORAR_RENDIMIENTO, RECOMPOSICION_CORPORAL, OTRO -> null;
        };
    }

    @Transactional
    public void actualizarPesoSemanal(Long clienteId, java.math.BigDecimal pesoKg, LocalDate fecha) {
        var cliente = clientes.findById(clienteId).orElseThrow(
                () -> new com.backend.nutri_predic.common.exception.ResourceNotFoundException("Cliente"));
        if (Objects.equals(cliente.getPesoKg(), pesoKg)) return;
        cliente.setPesoKg(pesoKg);
        clientes.save(cliente);
        var abiertos = historial.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(clienteId).stream()
                .filter(h -> h.getVigenteHasta() == null).toList();
        var snapshotMismoDia = abiertos.stream().filter(h -> fecha.equals(h.getFechaDesde())).findFirst().orElse(null);
        if (snapshotMismoDia != null) {
            snapshotMismoDia.setPesoKg(pesoKg);
            historial.save(snapshotMismoDia);
            return;
        }
        abiertos.forEach(h -> { h.setVigenteHasta(fecha); historial.save(h); });
        historial.save(new HistorialPerfilCliente(cliente, fecha));
    }

    @Transactional(readOnly = true)
    public List<HistorialPerfilResponse> historial(Long id, Authentication auth) {
        accessService.client(id, auth);
        return historial.findByClienteIdOrderByFechaDesdeDescCreadoEnDescIdDesc(id).stream()
                .map(HistorialPerfilResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public HistorialPerfilResponse perfilEnFecha(Long id, LocalDate fecha, Authentication auth) {
        accessService.client(id, auth);
        return historial
                .findFirstByClienteIdAndFechaDesdeLessThanEqualOrderByFechaDesdeDescCreadoEnDescIdDesc(
                        id, fecha)
                .map(HistorialPerfilResponse::from)
                .orElseThrow(
                        () ->
                                new com.backend.nutri_predic.common.exception
                                        .ResourceNotFoundException("Perfil histórico aplicable"));
    }
}
