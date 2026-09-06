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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SensorService {

    private final SensorRepository repository;
    private final LecturaRepository lecturaRepository;
    private final GraficoRepository graficoRepository;

    public SensorService(
        SensorRepository repository,
        LecturaRepository lecturaRepository,
        GraficoRepository graficoRepository
    ) {
        this.repository = repository;
        this.lecturaRepository = lecturaRepository;
        this.graficoRepository = graficoRepository;
    }

    public List<Sensor> listar() {
        return repository.findAll();
    }

    public Sensor buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Sensor no encontrado"
                        )
                );
    }

    public Sensor crear(SensorRequest r) {
        Sensor s = new Sensor(
                r.getNombre().trim(),
                r.getTipo().trim(),
                r.getUnidad().trim(),
                r.getEstado().trim().toUpperCase(),
                r.getValorActual()
        );

        s.setUltimaLectura(LocalDateTime.now());

        return repository.save(s);
    }

    public Sensor actualizar(Long id, SensorRequest r) {
        Sensor s = buscar(id);

        s.setNombre(r.getNombre().trim());
        s.setTipo(r.getTipo().trim());
        s.setUnidad(r.getUnidad().trim());
        s.setEstado(r.getEstado().trim().toUpperCase());
        s.setValorActual(r.getValorActual());
        s.setUltimaLectura(LocalDateTime.now());

        return repository.save(s);
    }

    public Sensor cambiarEstado(Long id, String estado) {
        Sensor s = buscar(id);

        if (!estado.equalsIgnoreCase("ACTIVO")
                && !estado.equalsIgnoreCase("INACTIVO")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado debe ser ACTIVO o INACTIVO"
            );
        }

        s.setEstado(estado.toUpperCase());

        return repository.save(s);
    }

    /**
     * Elimina un sensor y todos los datos que dependen de él.
     *
     * Orden de eliminación:
     *
     * 1. Elimina los gráficos asociados al sensor.
     * 2. Elimina las lecturas asociadas al sensor.
     * 3. Elimina finalmente el sensor.
     *
     * La operación es transaccional para evitar que la base de datos
     * quede en un estado inconsistente si ocurre algún error.
     */
    @Transactional
    public void eliminar(Long id) {

        Sensor sensor = buscar(id);

        // Eliminar primero los datos relacionados
        graficoRepository.deleteBySensorId(id);
        lecturaRepository.deleteBySensorId(id);

        // Finalmente eliminar el sensor
        repository.delete(sensor);
    }
}
