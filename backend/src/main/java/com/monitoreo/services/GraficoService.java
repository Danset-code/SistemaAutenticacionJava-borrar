package com.monitoreo.services;

import com.monitoreo.dto.GraficoRequest;
import com.monitoreo.models.Grafico;
import com.monitoreo.models.Sensor;
import com.monitoreo.repositories.GraficoRepository;
import com.monitoreo.repositories.SensorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class GraficoService {
    private final GraficoRepository graficoRepository;
    private final SensorRepository sensorRepository;

    public GraficoService(GraficoRepository graficoRepository, SensorRepository sensorRepository) {
        this.graficoRepository = graficoRepository;
        this.sensorRepository = sensorRepository;
    }

    public List<Grafico> listar() { return graficoRepository.findAll(); }
    public List<Grafico> listarActivos() { return graficoRepository.findByActivoTrueOrderByIdAsc(); }

    public Grafico buscar(Long id) {
        return graficoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gráfico no encontrado"));
    }

    public Grafico crear(GraficoRequest r) {
        Sensor sensor = sensorRepository.findById(r.getSensorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado"));
        Grafico g = new Grafico(r.getNombre().trim(), r.getTipo().trim().toUpperCase(),
                sensor, r.getActivo());
        return graficoRepository.save(g);
    }

    public Grafico actualizar(Long id, GraficoRequest r) {
        Grafico g = buscar(id);
        Sensor sensor = sensorRepository.findById(r.getSensorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado"));
        g.setNombre(r.getNombre().trim());
        g.setTipo(r.getTipo().trim().toUpperCase());
        g.setSensor(sensor);
        g.setActivo(r.getActivo());
        return graficoRepository.save(g);
    }

    public Grafico cambiarActivo(Long id, Boolean activo) {
        Grafico g = buscar(id);
        g.setActivo(activo);
        return graficoRepository.save(g);
    }

    public void eliminar(Long id) {
        graficoRepository.delete(buscar(id));
    }
}
