package com.backend.nutri_predic.actividadfisica.repository;
import com.backend.nutri_predic.actividadfisica.entity.MapeoCategoriaActividad;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MapeoCategoriaActividadRepository extends JpaRepository<MapeoCategoriaActividad,Long>{ List<MapeoCategoriaActividad> findAllByOrderByIdAsc(); }
