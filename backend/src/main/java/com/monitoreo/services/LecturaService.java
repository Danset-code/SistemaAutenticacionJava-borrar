package com.monitoreo.services;

import com.monitoreo.dto.LecturaRequest;
import com.monitoreo.models.Lectura;
import com.monitoreo.models.Sensor;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.SensorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LecturaService {

    private final LecturaRepository lecturaRepository;
    private final SensorRepository sensorRepository;

    public LecturaService(LecturaRepository lecturaRepository, SensorRepository sensorRepository) {
        this.lecturaRepository = lecturaRepository;
        this.sensorRepository = sensorRepository;
    }

    public List<Lectura> listar() {
        return lecturaRepository.findAllByOrderByFechaDesc();
    }

    public Lectura buscar(Long id) {
        return lecturaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lectura no encontrada"));
    }

    public List<Lectura> porSensor(Long sensorId) {
        return lecturaRepository.findBySensorIdOrderByFechaDesc(sensorId);
    }

    public Lectura crear(LecturaRequest r) {
        Sensor sensor = sensorRepository.findById(r.getSensorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado"));

        LocalDateTime fecha = r.getFecha() == null ? LocalDateTime.now() : r.getFecha();
        Lectura l = new Lectura(sensor, r.getValor(), fecha);
        sensor.setValorActual(r.getValor());
        sensor.setUltimaLectura(fecha);
        sensorRepository.save(sensor);
        return lecturaRepository.save(l);
    }

    public void eliminar(Long id) {
        lecturaRepository.delete(buscar(id));
    }

    public List<Lectura> reporte(Long sensorId, LocalDateTime desde, LocalDateTime hasta) {

        // Hay sensor y rango de fechas
        if (sensorId != null && desde != null && hasta != null) {
            return lecturaRepository.findTop50BySensorIdAndFechaBetweenOrderByFechaDesc(
                    sensorId,
                    desde,
                    hasta
            );
        }

        // Solo rango de fechas, todos los sensores
        if (sensorId == null && desde != null && hasta != null) {
            return lecturaRepository.findTop50ByFechaBetweenOrderByFechaDesc(
                    desde,
                    hasta
            );
        }

        // Solo un sensor
        if (sensorId != null) {
            return lecturaRepository.findTop50BySensorIdOrderByFechaDesc(sensorId);
        }

        // Todos los sensores
        return lecturaRepository.findTop50ByOrderByFechaDesc();
    }
}
