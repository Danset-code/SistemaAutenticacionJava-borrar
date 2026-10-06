package com.monitoreo.services;

import com.monitoreo.dto.SensorRequest;
import com.monitoreo.models.Sensor;
import com.monitoreo.repositories.GraficoRepository;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.SensorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SensorService {

    private final SensorRepository repository;
    private final LecturaRepository lecturaRepository;
    private final GraficoRepository graficoRepository;

    public SensorService(
            SensorRepository repository,
            LecturaRepository lecturaRepository,
            GraficoRepository graficoRepository) {
        this.repository = repository;
        this.lecturaRepository = lecturaRepository;
        this.graficoRepository = graficoRepository;
    }

    public List<Sensor> listar() { return repository.findAll(); }

    public Sensor buscar(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado"));
    }

    public Sensor buscarPorDispositivo(String deviceId, String canal) {
        if (deviceId == null || deviceId.isBlank() || canal == null || canal.isBlank()) return null;
        return repository.findByDeviceIdAndCanal(deviceId.trim(), canal.trim()).orElse(null);
    }

    public Sensor crear(SensorRequest r) {
        String deviceId = normalize(r.getDeviceId());
        String canal = normalize(r.getCanal());

        if (deviceId != null && canal != null && repository.existsByDeviceIdAndCanal(deviceId, canal)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un sensor con ese deviceId y canal");
        }

        Sensor s = new Sensor(
                r.getNombre().trim(),
                r.getTipo().trim(),
                r.getUnidad().trim(),
                normalizeState(r.getEstado()),
                r.getValorActual());

        s.setDeviceId(deviceId);
        s.setCanal(canal);
        // Un sensor recién creado no se considera conectado hasta recibir una lectura real.
        s.setUltimaLectura(null);
        s.setEstado("INACTIVO");

        return repository.save(s);
    }

    public Sensor actualizar(Long id, SensorRequest r) {
        Sensor s = buscar(id);
        s.setNombre(r.getNombre().trim());
        s.setTipo(r.getTipo().trim());
        s.setUnidad(r.getUnidad().trim());
        if (r.getDeviceId() != null && !r.getDeviceId().isBlank()) s.setDeviceId(r.getDeviceId().trim());
        if (r.getCanal() != null && !r.getCanal().isBlank()) s.setCanal(r.getCanal().trim());
        s.setValorActual(r.getValorActual());
        // El estado se conserva/calcula por comunicación, no por la petición de edición.
        return repository.save(s);
    }

    @Transactional
    public void eliminar(Long id) {
        Sensor sensor = buscar(id);
        graficoRepository.deleteBySensorId(id);
        lecturaRepository.deleteBySensorId(id);
        repository.delete(sensor);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeState(String state) {
        return "ACTIVO".equalsIgnoreCase(state) ? "ACTIVO" : "INACTIVO";
    }
}
