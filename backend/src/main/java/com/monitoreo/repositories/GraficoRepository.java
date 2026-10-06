package com.monitoreo.repositories;

import com.monitoreo.models.Grafico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GraficoRepository extends JpaRepository<Grafico, Long> {

    List<Grafico> findByActivoTrueOrderByIdAsc();

    // Busca todos los gráficos asociados a un sensor
    List<Grafico> findBySensorId(Long sensorId);

    // Elimina todos los gráficos asociados a un sensor
    void deleteBySensorId(Long sensorId);

    long countBySensorId(Long sensorId);

    java.util.Optional<Grafico> findFirstBySensorIdOrderByIdAsc(Long sensorId);
    boolean existsBySensorId(Long sensorId);
}
