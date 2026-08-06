package com.monitoreo.repositories;

import com.monitoreo.models.Lectura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LecturaRepository extends JpaRepository<Lectura, Long> {

    // ==============================
    // CONSULTAS GENERALES
    // ==============================

    List<Lectura> findAllByOrderByFechaDesc();

    List<Lectura> findBySensorIdOrderByFechaDesc(Long sensorId);


    // ==============================
    // REPORTES - MÁXIMO 50
    // ==============================

    List<Lectura> findTop50ByOrderByFechaDesc();

    List<Lectura> findTop50BySensorIdOrderByFechaDesc(Long sensorId);


    // ==============================
    // CONSULTAS POR FECHA
    // ==============================

    List<Lectura> findByFechaBetweenOrderByFechaDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Lectura> findBySensorIdAndFechaBetweenOrderByFechaDesc(
            Long sensorId,
            LocalDateTime desde,
            LocalDateTime hasta
    );


    // ==============================
    // REPORTES - FECHA + MÁXIMO 50
    // ==============================

    List<Lectura> findTop50ByFechaBetweenOrderByFechaDesc(
            LocalDateTime desde,
            LocalDateTime hasta
    );

    List<Lectura> findTop50BySensorIdAndFechaBetweenOrderByFechaDesc(
            Long sensorId,
            LocalDateTime desde,
            LocalDateTime hasta
    );
}
