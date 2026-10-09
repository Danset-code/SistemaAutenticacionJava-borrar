package com.monitoreo.repositories;

import com.monitoreo.models.Grafico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GraficoRepository extends JpaRepository<Grafico, Long> {
    List<Grafico> findByActivoTrueOrderByIdAsc();
    List<Grafico> findBySensorId(Long sensorId);
    void deleteBySensorId(Long sensorId);
    long countBySensorId(Long sensorId);
    Optional<Grafico> findFirstBySensorIdOrderByIdAsc(Long sensorId);
    boolean existsBySensorId(Long sensorId);
    List<Grafico> findBySensor_Usuario_IdOrderByIdAsc(Long usuarioId);
    List<Grafico> findByActivoTrueAndSensor_Usuario_IdOrderByIdAsc(Long usuarioId);
    Optional<Grafico> findByIdAndSensor_Usuario_Id(Long id, Long usuarioId);
}
