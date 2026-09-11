package com.backend.nutri_predic.requerimientonutricional.repository;
import com.backend.nutri_predic.requerimientonutricional.entity.RequerimientoNutricionalRegla;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RequerimientoNutricionalReglaRepository extends JpaRepository<RequerimientoNutricionalRegla, Long> {
    List<RequerimientoNutricionalRegla> findAllByOrderByIdAsc();
}
