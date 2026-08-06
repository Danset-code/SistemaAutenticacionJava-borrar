package com.monitoreo.services;

import com.monitoreo.models.Lectura;
import com.monitoreo.models.Sensor;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.websocket.SensorWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class SensorSimulator {
    private final SensorRepository sensorRepository;
    private final LecturaRepository lecturaRepository;
    private final SensorWebSocketHandler webSocketHandler;
    private final Random random = new Random();

    public SensorSimulator(SensorRepository sensorRepository,
                           LecturaRepository lecturaRepository,
                           SensorWebSocketHandler webSocketHandler) {
        this.sensorRepository = sensorRepository;
        this.lecturaRepository = lecturaRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRate = 5000)
    public void generarLecturas() {
        List<Sensor> sensores = sensorRepository.findAll();

        for (Sensor sensor : sensores) {
            if (!"ACTIVO".equalsIgnoreCase(sensor.getEstado())) {
                continue;
            }

            double valor = generarValor(sensor);
            LocalDateTime fecha = LocalDateTime.now();

            sensor.setValorActual(valor);
            sensor.setUltimaLectura(fecha);
            sensorRepository.save(sensor);

            lecturaRepository.save(new Lectura(sensor, valor, fecha));

            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("tipo", "lectura");
            mensaje.put("sensorId", sensor.getId());
            mensaje.put("sensor", sensor.getTipo());
            mensaje.put("nombre", sensor.getNombre());
            mensaje.put("valor", Math.round(valor * 10.0) / 10.0);
            mensaje.put("unidad", sensor.getUnidad());
            mensaje.put("fecha", fecha.toString());

            webSocketHandler.broadcast(mensaje);
        }
    }

    private double generarValor(Sensor sensor) {
        return switch (sensor.getTipo().toLowerCase()) {
            case "temperatura" -> 24.0 + random.nextDouble() * 7.0;
            case "humedad" -> 60.0 + random.nextDouble() * 20.0;
            case "co₂", "co2" -> 380.0 + random.nextDouble() * 120.0;
            default -> 10.0 + random.nextDouble() * 90.0;
        };
    }
}
