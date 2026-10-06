package com.monitoreo.repositories;

import com.monitoreo.models.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByDeviceIdAndCanal(String deviceId, String canal);
    boolean existsByDeviceIdAndCanal(String deviceId, String canal);
}
