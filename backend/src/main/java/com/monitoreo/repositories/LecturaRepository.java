
package com.monitoreo.repositories;

import com.monitoreo.models.Lectura;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    // =====================================================
    // Métodos existentes
    // =====================================================

    List<Lectura> findAllByOrderByFechaDesc();

    List<Lectura> findBySensorIdOrderByFechaDesc(Long sensorId);

    void deleteBySensorId(Long sensorId);

    List<Lectura> findTop50ByOrderByFechaDesc();

    List<Lectura> findTop50BySensorIdOrderByFechaDesc(Long sensorId);

    List<Lectura> findByFechaBetweenOrderByFechaDesc(
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Lectura> findBySensorIdAndFechaBetweenOrderByFechaDesc(
            Long sensorId,
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Lectura> findTop50ByFechaBetweenOrderByFechaDesc(
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Lectura> findTop50BySensorIdAndFechaBetweenOrderByFechaDesc(
            Long sensorId,
            LocalDateTime desde,
            LocalDateTime hasta);

    // =====================================================
    // Consultas aisladas por propietario
    // =====================================================

    List<Lectura> findAllBySensor_Usuario_IdOrderByFechaDesc(
            Long usuarioId);

    Optional<Lectura> findByIdAndSensor_Usuario_Id(
            Long id,
            Long usuarioId);

    List<Lectura> findBySensor_IdAndSensor_Usuario_IdOrderByFechaDesc(
            Long sensorId,
            Long usuarioId);

    List<Lectura> findBySensor_IdAndSensor_Usuario_IdAndFechaBetweenOrderByFechaDesc(
            Long sensorId,
            Long usuarioId,
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Lectura> findBySensor_Usuario_IdAndFechaBetweenOrderByFechaDesc(
            Long usuarioId,
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Lectura> findTop50BySensor_Usuario_IdOrderByFechaDesc(
            Long usuarioId);

    List<Lectura> findTop50BySensor_IdAndSensor_Usuario_IdOrderByFechaDesc(
            Long sensorId,
            Long usuarioId);

    List<Lectura> findTop50BySensor_IdAndSensor_Usuario_IdAndFechaBetweenOrderByFechaDesc(
            Long sensorId,
            Long usuarioId,
            LocalDateTime desde,
            LocalDateTime hasta);

    List<Lectura> findTop50BySensor_Usuario_IdAndFechaBetweenOrderByFechaDesc(
            Long usuarioId,
            LocalDateTime desde,
            LocalDateTime hasta);

    // =====================================================
    // Consulta dinámica para reportes y exportación
    // =====================================================

    @Query("""
        SELECT l
        FROM Lectura l
        WHERE l.sensor.usuario.id = :usuarioId
          AND (:sensorId IS NULL OR l.sensor.id = :sensorId)
          AND (:deviceId IS NULL OR l.sensor.deviceId = :deviceId)
          AND (:desde IS NULL OR l.fecha >= :desde)
          AND (:hastaExclusivo IS NULL OR l.fecha < :hastaExclusivo)
        ORDER BY l.fecha DESC
        """)
    List<Lectura> buscarReporte(
            @Param("usuarioId") Long usuarioId,
            @Param("sensorId") Long sensorId,
            @Param("deviceId") String deviceId,
            @Param("desde") LocalDateTime desde,
            @Param("hastaExclusivo") LocalDateTime hastaExclusivo,
            Pageable pageable
    );
}