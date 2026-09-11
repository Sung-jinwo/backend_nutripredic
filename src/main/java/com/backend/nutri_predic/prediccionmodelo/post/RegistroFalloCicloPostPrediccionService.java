package com.backend.nutri_predic.prediccionmodelo.post;

import com.backend.nutri_predic.prediccionmodelo.repository.PrediccionModeloRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class RegistroFalloCicloPostPrediccionService {
    private final FalloCicloPostPrediccionRepository fallos;
    private final PrediccionModeloRepository predicciones;
    public RegistroFalloCicloPostPrediccionService(FalloCicloPostPrediccionRepository fallos, PrediccionModeloRepository predicciones) { this.fallos = fallos; this.predicciones = predicciones; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long prediccionId, String modulo, RuntimeException error) {
        var fallo = new FalloCicloPostPrediccion();
        fallo.setPrediccionModelo(predicciones.getReferenceById(prediccionId));
        fallo.setModulo(modulo); fallo.setTipoError(error.getClass().getSimpleName());
        fallo.setDetalle(error.getMessage() == null ? "Fallo sin detalle" : error.getMessage().substring(0, Math.min(1000, error.getMessage().length())));
        fallos.save(fallo);
    }
}
