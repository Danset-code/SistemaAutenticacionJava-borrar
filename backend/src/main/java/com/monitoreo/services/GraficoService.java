package com.monitoreo.services;

import com.monitoreo.dto.GraficoRequest;
import com.monitoreo.models.Grafico;
import com.monitoreo.models.Sensor;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.GraficoRepository;
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
    public GraficoService(GraficoRepository graficoRepository, SensorRepository sensorRepository) {
        this.graficoRepository=graficoRepository; this.sensorRepository=sensorRepository;
    }
    public List<Grafico> listar(Usuario owner) { return graficoRepository.findBySensor_Usuario_IdOrderByIdAsc(owner.getId()); }
    public List<Grafico> listarActivos(Usuario owner) { return graficoRepository.findByActivoTrueAndSensor_Usuario_IdOrderByIdAsc(owner.getId()); }
    public Grafico buscar(Long id, Usuario owner) {
        return graficoRepository.findByIdAndSensor_Usuario_Id(id, owner.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Gráfico no encontrado"));
    }
    private Sensor sensorPropio(Long sensorId, Usuario owner) {
        return sensorRepository.findByIdAndUsuario_Id(sensorId, owner.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado en tu cuenta"));
    }
    public Grafico crear(GraficoRequest r, Usuario owner) {
        Sensor sensor=sensorPropio(r.getSensorId(), owner);
        if (graficoRepository.existsBySensorId(sensor.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este sensor ya tiene un gráfico asociado");
        return graficoRepository.save(new Grafico(r.getNombre().trim(), r.getTipo().trim().toUpperCase(), sensor, r.getActivo()));
    }
    public Grafico actualizar(Long id, GraficoRequest r, Usuario owner) {
        Grafico g=buscar(id, owner); Sensor sensor=sensorPropio(r.getSensorId(), owner);
        if (graficoRepository.existsBySensorId(sensor.getId()) && !sensor.getId().equals(g.getSensor().getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este sensor ya tiene un gráfico asociado");
        g.setNombre(r.getNombre().trim()); g.setTipo(r.getTipo().trim().toUpperCase()); g.setSensor(sensor); g.setActivo(r.getActivo());
        return graficoRepository.save(g);
    }
    public Grafico cambiarActivo(Long id, Boolean activo, Usuario owner) { Grafico g=buscar(id, owner); g.setActivo(activo); return graficoRepository.save(g); }
    @Transactional
    public void eliminar(Long id, Usuario owner) { Grafico grafico=buscar(id, owner); graficoRepository.delete(grafico); graficoRepository.flush(); }
}
