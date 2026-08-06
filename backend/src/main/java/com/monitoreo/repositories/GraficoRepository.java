package com.monitoreo.repositories;

import com.monitoreo.models.Grafico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GraficoRepository extends JpaRepository<Grafico, Long> {
    List<Grafico> findByActivoTrueOrderByIdAsc();
}
