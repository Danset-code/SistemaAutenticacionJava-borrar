package com.monitoreo.services;

import com.monitoreo.dto.GraficoRequest;
import com.monitoreo.models.Grafico;
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
public class GraficoService {
    private final GraficoRepository graficoRepository;
    private final SensorRepository sensorRepository;
    private final LecturaRepository lecturaRepository;

    public GraficoService(
            GraficoRepository graficoRepository,
            SensorRepository sensorRepository,
            LecturaRepository lecturaRepository) {
        this.graficoRepository = graficoRepository;
        this.sensorRepository = sensorRepository;
        this.lecturaRepository = lecturaRepository;
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

    /**
     * Elimina el gráfico y, cuando ese sensor ya no tiene ningún otro gráfico,
     * elimina también sus lecturas y el sensor. Así la sección "Sensores"
     * representa exactamente los sensores que tienen un gráfico configurado.
     */
    @Transactional
    public void eliminar(Long id) {
        Grafico grafico = buscar(id);
        Long sensorId = grafico.getSensor().getId();

        graficoRepository.delete(grafico);
        graficoRepository.flush();

        if (graficoRepository.countBySensorId(sensorId) == 0) {
            lecturaRepository.deleteBySensorId(sensorId);
            sensorRepository.deleteById(sensorId);
        }
    }
}
