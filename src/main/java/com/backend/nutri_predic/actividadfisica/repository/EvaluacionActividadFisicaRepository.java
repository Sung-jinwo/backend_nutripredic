package com.backend.nutri_predic.actividadfisica.repository;
import com.backend.nutri_predic.actividadfisica.entity.EvaluacionActividadFisica;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EvaluacionActividadFisicaRepository extends JpaRepository<EvaluacionActividadFisica,Long>{ List<EvaluacionActividadFisica> findByClienteIdOrderByFechaEvaluacionDesc(Long clienteId); }
