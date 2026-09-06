package com.monitoreo.services;

import com.monitoreo.dto.TipoSensorRequest;
import com.monitoreo.models.TipoSensor;
import com.monitoreo.repositories.TipoSensorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TipoSensorService {
    private final TipoSensorRepository repository;

    public TipoSensorService(TipoSensorRepository repository) {
        this.repository = repository;
    }

    public List<TipoSensor> listar(boolean soloActivos) {
        return soloActivos ? repository.findByActivoTrueOrderByNombreAsc() : repository.findAll();
    }

    public TipoSensor buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de sensor no encontrado"));
    }

    public TipoSensor crear(TipoSensorRequest request) {
        String nombre = request.getNombre().trim();
        if (repository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe ese tipo de sensor en el catálogo");
        }
        return repository.save(new TipoSensor(nombre, request.getUnidad().trim(), request.getCantidadDisponible()));
    }

    public TipoSensor actualizar(Long id, TipoSensorRequest request) {
        TipoSensor tipo = buscar(id);
        String nombre = request.getNombre().trim();

        if (!tipo.getNombre().equalsIgnoreCase(nombre) && repository.existsByNombreIgnoreCase(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe ese tipo de sensor en el catálogo");
        }

        tipo.setNombre(nombre);
        tipo.setUnidad(request.getUnidad().trim());
        tipo.setCantidadDisponible(request.getCantidadDisponible());
        tipo.setActivo(request.getActivo() == null || request.getActivo());
        return repository.save(tipo);
    }

    public TipoSensor cambiarActivo(Long id, boolean activo) {
        TipoSensor tipo = buscar(id);
        tipo.setActivo(activo);
        return repository.save(tipo);
    }

    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }
}
