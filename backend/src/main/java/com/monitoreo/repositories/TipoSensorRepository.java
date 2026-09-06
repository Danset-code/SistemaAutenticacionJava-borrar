package com.monitoreo.repositories;

import com.monitoreo.models.TipoSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoSensorRepository extends JpaRepository<TipoSensor, Long> {
    boolean existsByNombreIgnoreCase(String nombre);
    List<TipoSensor> findByActivoTrueOrderByNombreAsc();
}
