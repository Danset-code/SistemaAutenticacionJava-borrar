
package com.monitoreo.services;

import com.monitoreo.dto.LecturaRequest;
import com.monitoreo.models.Dispositivo;
import com.monitoreo.models.Lectura;
import com.monitoreo.models.Sensor;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.websocket.SensorWebSocketHandler;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final DispositivoService dispositivoService;

    public LecturaService(
            LecturaRepository lecturaRepository,
            SensorRepository sensorRepository,
            SensorWebSocketHandler webSocketHandler,
            DispositivoService dispositivoService) {

        this.lecturaRepository = lecturaRepository;
        this.sensorRepository = sensorRepository;
        this.webSocketHandler = webSocketHandler;
        this.dispositivoService = dispositivoService;
    }

    public List<Lectura> listar(Usuario owner) {
        validarUsuario(owner);

        return lecturaRepository
                .findAllBySensor_Usuario_IdOrderByFechaDesc(owner.getId());
    }

    public Lectura buscar(Long id, Usuario owner) {
        validarUsuario(owner);

        return lecturaRepository
                .findByIdAndSensor_Usuario_Id(id, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lectura no encontrada"
                ));
    }

    public List<Lectura> porSensor(Long sensorId, Usuario owner) {
        validarUsuario(owner);
        validarSensor(sensorId, owner);

        return lecturaRepository
                .findBySensor_IdAndSensor_Usuario_IdOrderByFechaDesc(
                        sensorId,
                        owner.getId()
                );
    }

    @Transactional
    public Lectura crear(
            LecturaRequest r,
            Usuario owner,
            Dispositivo sourceDevice) {

        validarUsuario(owner);

        Sensor sensor = resolveSensor(r, owner, sourceDevice);

        LocalDateTime fecha = r.getFecha() == null
                ? LocalDateTime.now()
                : r.getFecha();

        Lectura l = new Lectura(sensor, r.getValor(), fecha);

        sensor.setValorActual(r.getValor());
        sensor.setUltimaLectura(fecha);
        sensor.setEstado("ACTIVO");

        sensorRepository.save(sensor);

        Lectura saved = lecturaRepository.save(l);

        webSocketHandler.broadcast(
                lecturePayload(saved),
                owner.getId()
        );

        return saved;
    }

    private Sensor resolveSensor(
            LecturaRequest r,
            Usuario owner,
            Dispositivo sourceDevice) {

        if (sourceDevice != null) {
            String requested = trim(r.getDeviceId());

            if (requested != null
                    && !requested.equals(sourceDevice.getHardwareId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El hardwareId no corresponde a la clave del dispositivo"
                );
            }

            r.setDeviceId(sourceDevice.getHardwareId());
        }

        if (r.getSensorId() != null) {
            if (sourceDevice != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El dispositivo debe enviar hardwareId + canal, no sensorId"
                );
            }

            return validarSensor(r.getSensorId(), owner);
        }

        String deviceId = trim(r.getDeviceId());
        String canal = trim(r.getCanal());

        if (deviceId == null || canal == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe enviar sensorId o hardwareId + canal"
            );
        }

        Dispositivo dispositivo;

        if (sourceDevice != null) {
            dispositivo = sourceDevice;
        } else {
            dispositivo = dispositivoService
                    .asegurarDispositivoLegacy(owner, deviceId);
        }

        if (!dispositivo.getUsuario().getId().equals(owner.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El dispositivo pertenece a otra cuenta"
            );
        }

        Sensor sensor = sensorRepository
                .findByDeviceIdAndCanalAndUsuario_Id(
                        deviceId,
                        canal,
                        owner.getId()
                )
                .orElse(null);

        if (sensor != null) {
            return sensor;
        }

        String tipo = trim(r.getTipo());
        String unidad = trim(r.getUnidad());

        if (tipo == null || unidad == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Para crear el sensor automáticamente debe enviar tipo y unidad"
            );
        }

        Sensor nuevo = new Sensor(
                tipo + " (" + dispositivo.getAlias() + " · canal " + canal + ")",
                tipo,
                unidad,
                "ACTIVO",
                r.getValor()
        );

        nuevo.setDeviceId(deviceId);
        nuevo.setDeviceAlias(dispositivo.getAlias());
        nuevo.setCanal(canal);
        nuevo.setUsuario(owner);
        nuevo.setEstado("ACTIVO");
        nuevo.setUltimaLectura(
                r.getFecha() == null ? LocalDateTime.now() : r.getFecha()
        );

        return sensorRepository.save(nuevo);
    }

    private Sensor validarSensor(Long sensorId, Usuario owner) {
        return sensorRepository
                .findByIdAndUsuario_Id(sensorId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sensor no encontrado en tu cuenta"
                ));
    }

    private Map<String, Object> lecturePayload(Lectura lectura) {
        Sensor s = lectura.getSensor();

        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("event", "LECTURA");
        payload.put("sensorId", s.getId());
        payload.put("deviceId", s.getDeviceId());
        payload.put("deviceAlias", s.getDeviceAlias());
        payload.put("canal", s.getCanal());
        payload.put("tipo", s.getTipo());
        payload.put("unidad", s.getUnidad());
        payload.put("valor", lectura.getValor());
        payload.put("fecha", lectura.getFecha());
        payload.put("estado", s.getEstado());

        return payload;
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void validarUsuario(Usuario owner) {
        if (owner == null || owner.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "La sesión no es válida"
            );
        }
    }

    private void validarFiltros(
            Long sensorId,
            LocalDateTime desde,
            LocalDateTime hastaExclusivo,
            Usuario owner) {

        validarUsuario(owner);

        if (sensorId != null) {
            validarSensor(sensorId, owner);
        }

        if (desde != null
                && hastaExclusivo != null
                && !desde.isBefore(hastaExclusivo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El rango de fechas no es válido"
            );
        }
    }

    private String normalizarDeviceId(String deviceId) {
        return deviceId == null || deviceId.isBlank()
                ? null
                : deviceId.trim();
    }

    @Transactional
    public void eliminar(Long id, Usuario owner) {
        lecturaRepository.delete(buscar(id, owner));
    }

    /**
     * Recupera TODOS los registros que cumplan los filtros.
     * Se utiliza para generar el archivo Excel desde MySQL.
     */
    public List<Lectura> reporteCompleto(
            Long sensorId,
            String deviceId,
            LocalDateTime desde,
            LocalDateTime hastaExclusivo,
            Usuario owner) {

        validarFiltros(sensorId, desde, hastaExclusivo, owner);

        return lecturaRepository.buscarReporte(
                owner.getId(),
                sensorId,
                normalizarDeviceId(deviceId),
                desde,
                hastaExclusivo,
                Pageable.unpaged()
        );
    }

    /**
     * Recupera como máximo los 50 registros más recientes
     * que cumplan los mismos filtros.
     */
    public List<Lectura> reporte(
            Long sensorId,
            String deviceId,
            LocalDateTime desde,
            LocalDateTime hastaExclusivo,
            Usuario owner) {

        validarFiltros(sensorId, desde, hastaExclusivo, owner);

        return lecturaRepository.buscarReporte(
                owner.getId(),
                sensorId,
                normalizarDeviceId(deviceId),
                desde,
                hastaExclusivo,
                PageRequest.of(0, 50)
        );
    }
}