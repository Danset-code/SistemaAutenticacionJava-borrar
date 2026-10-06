package com.monitoreo.services;

import com.monitoreo.dto.LecturaRequest;
import com.monitoreo.models.Lectura;
import com.monitoreo.models.Sensor;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.websocket.SensorWebSocketHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LecturaService {

    private final LecturaRepository lecturaRepository;
    private final SensorRepository sensorRepository;
    private final SensorWebSocketHandler webSocketHandler;

    public LecturaService(
            LecturaRepository lecturaRepository,
            SensorRepository sensorRepository,
            SensorWebSocketHandler webSocketHandler) {
        this.lecturaRepository = lecturaRepository;
        this.sensorRepository = sensorRepository;
        this.webSocketHandler = webSocketHandler;
    }

    public List<Lectura> listar() { return lecturaRepository.findAllByOrderByFechaDesc(); }

    public Lectura buscar(Long id) {
        return lecturaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lectura no encontrada"));
    }

    public List<Lectura> porSensor(Long sensorId) {
        return lecturaRepository.findBySensorIdOrderByFechaDesc(sensorId);
    }

    @Transactional
    public Lectura crear(LecturaRequest r) {

        Sensor sensor = resolveSensor(r);

        LocalDateTime fecha =
                r.getFecha() == null
                        ? LocalDateTime.now()
                        : r.getFecha();

        Lectura l = new Lectura(
                sensor,
                r.getValor(),
                fecha
        );

        /*
         * La lectura actualiza directamente el sensor.
         * Esto NO depende de que exista un gráfico.
         */
        sensor.setValorActual(r.getValor());
        sensor.setUltimaLectura(fecha);
        sensor.setEstado("ACTIVO");

        sensorRepository.save(sensor);

        /*
         * Primero guardamos la lectura en BD.
         */
        Lectura saved = lecturaRepository.save(l);

        /*
         * Después notificamos al frontend.
         *
         * IMPORTANTE:
         * aquí ya NO buscamos ningún gráfico.
         * El WebSocket representa una lectura de un sensor,
         * no una lectura de un gráfico.
         */
        webSocketHandler.broadcast(
                lecturePayload(saved)
        );

        return saved;
    }

    private Sensor resolveSensor(LecturaRequest r) {
        if (r.getSensorId() != null) {
            return sensorRepository.findById(r.getSensorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado"));
        }

        String deviceId = trim(r.getDeviceId());
        String canal = trim(r.getCanal());
        if (deviceId == null || canal == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe enviar sensorId o deviceId + canal");
        }

        Sensor sensor = sensorRepository.findByDeviceIdAndCanal(deviceId, canal).orElse(null);
        if (sensor != null) return sensor;

        String tipo = trim(r.getTipo());
        String unidad = trim(r.getUnidad());
        if (tipo == null || unidad == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Para un sensor nuevo debe enviar tipo y unidad");
        }

        Sensor nuevo = new Sensor(
                "Sensor " + tipo + " " + deviceId + "-" + canal,
                tipo,
                unidad,
                "ACTIVO",
                r.getValor());
        nuevo.setDeviceId(deviceId);
        nuevo.setCanal(canal);
        nuevo.setEstado("ACTIVO");
        return sensorRepository.save(nuevo);
    }

    private Map<String, Object> lecturePayload(Lectura lectura) {

        Sensor s = lectura.getSensor();

        Map<String, Object> payload =
                new LinkedHashMap<>();

        payload.put("event", "LECTURA");

        payload.put("sensorId", s.getId());
        payload.put("deviceId", s.getDeviceId());
        payload.put("canal", s.getCanal());

        payload.put("tipo", s.getTipo());
        payload.put("unidad", s.getUnidad());

        payload.put("valor", lectura.getValor());
        payload.put("fecha", lectura.getFecha());

        payload.put("estado", s.getEstado());

        return payload;
    }

    private String trim(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public void eliminar(Long id) { lecturaRepository.delete(buscar(id)); }

    public List<Lectura> reporteCompleto(Long sensorId, LocalDateTime desde, LocalDateTime hasta) {
        if (sensorId != null && desde != null && hasta != null) return lecturaRepository.findBySensorIdAndFechaBetweenOrderByFechaDesc(sensorId, desde, hasta);
        if (sensorId != null) return lecturaRepository.findBySensorIdOrderByFechaDesc(sensorId);
        if (desde != null && hasta != null) return lecturaRepository.findByFechaBetweenOrderByFechaDesc(desde, hasta);
        return lecturaRepository.findAllByOrderByFechaDesc();
    }

    public List<Lectura> reporte(Long sensorId, LocalDateTime desde, LocalDateTime hasta) {
        if (sensorId != null && desde != null && hasta != null) return lecturaRepository.findTop50BySensorIdAndFechaBetweenOrderByFechaDesc(sensorId, desde, hasta);
        if (sensorId != null) return lecturaRepository.findTop50BySensorIdOrderByFechaDesc(sensorId);
        if (desde != null && hasta != null) return lecturaRepository.findTop50ByFechaBetweenOrderByFechaDesc(desde, hasta);
        return lecturaRepository.findTop50ByOrderByFechaDesc();
    }
}
