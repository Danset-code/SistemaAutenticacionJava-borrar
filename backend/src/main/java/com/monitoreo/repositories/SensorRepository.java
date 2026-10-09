package com.monitoreo.repositories;

import com.monitoreo.models.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByDeviceIdAndCanal(String deviceId, String canal);
    boolean existsByDeviceIdAndCanal(String deviceId, String canal);
    List<Sensor> findAllByUsuario_IdOrderByIdAsc(Long usuarioId);
    List<Sensor> findByUsuarioIsNull();
    Optional<Sensor> findByIdAndUsuario_Id(Long id, Long usuarioId);
    Optional<Sensor> findByDeviceIdAndCanalAndUsuario_Id(String deviceId, String canal, Long usuarioId);
    boolean existsByDeviceIdAndCanalAndUsuario_Id(String deviceId, String canal, Long usuarioId);
}
