package com.monitoreo.services;

import com.monitoreo.models.Sensor;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.websocket.SensorWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SensorEstadoScheduler {

    private static final long TIMEOUT_SECONDS = 10;

    private final SensorRepository sensorRepository;
    private final SensorWebSocketHandler webSocketHandler;

    public SensorEstadoScheduler(
            SensorRepository sensorRepository,
            SensorWebSocketHandler webSocketHandler) {

        this.sensorRepository = sensorRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void verificarEstados() {

        LocalDateTime ahora =
                LocalDateTime.now(ZoneId.of("America/Bogota"));

        List<Sensor> sensores = sensorRepository.findAll();

        for (Sensor sensor : sensores) {

            if (sensor.getUltimaLectura() == null) {
                continue;
            }

            long segundos = Duration.between(
                    sensor.getUltimaLectura(),
                    ahora
            ).getSeconds();

            boolean activo = segundos <= TIMEOUT_SECONDS;

            String nuevoEstado =
                    activo ? "ACTIVO" : "INACTIVO";

            if (!nuevoEstado.equalsIgnoreCase(sensor.getEstado())) {

                sensor.setEstado(nuevoEstado);

                sensorRepository.save(sensor);

                System.out.println(
                        "[ESTADO CAMBIO] Sensor=" +
                        sensor.getId() +
                        " -> " +
                        nuevoEstado +
                        " | segundos=" +
                        segundos
                );

                broadcastEstado(sensor);
            }
        }
    }

    private void broadcastEstado(Sensor sensor) {

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put("event", "ESTADO_SENSOR");
        payload.put("sensorId", sensor.getId());
        payload.put("deviceId", sensor.getDeviceId());
        payload.put("canal", sensor.getCanal());
        payload.put("tipo", sensor.getTipo());
        payload.put("unidad", sensor.getUnidad());
        payload.put("estado", sensor.getEstado());
        payload.put("valor", sensor.getValorActual());
        payload.put("fecha", sensor.getUltimaLectura());

        webSocketHandler.broadcast(payload);
    }
}
