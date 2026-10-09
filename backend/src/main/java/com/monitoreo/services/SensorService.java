package com.monitoreo.services;

import com.monitoreo.dto.SensorRequest;
import com.monitoreo.models.Dispositivo;
import com.monitoreo.models.Sensor;
import com.monitoreo.models.Usuario;
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
    private final DispositivoService dispositivoService;

    public SensorService(SensorRepository repository, LecturaRepository lecturaRepository,
                         GraficoRepository graficoRepository, DispositivoService dispositivoService) {
        this.repository=repository; this.lecturaRepository=lecturaRepository;
        this.graficoRepository=graficoRepository; this.dispositivoService=dispositivoService;
    }

    public List<Sensor> listar(Usuario owner) { return repository.findAllByUsuario_IdOrderByIdAsc(owner.getId()); }

    public Sensor buscar(Long id, Usuario owner) {
        return repository.findByIdAndUsuario_Id(id, owner.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Sensor no encontrado"));
    }

    public Sensor buscarPorDispositivo(String deviceId, String canal, Usuario owner) {
        if (deviceId == null || deviceId.isBlank() || canal == null || canal.isBlank()) return null;
        return repository.findByDeviceIdAndCanalAndUsuario_Id(deviceId.trim(), canal.trim(), owner.getId()).orElse(null);
    }

    @Transactional
    public Sensor crear(SensorRequest r, Usuario owner) {
        String deviceId=normalize(r.getDeviceId());
        String canal=normalize(r.getCanal());
        Dispositivo dispositivo=null;
        if (deviceId != null) {
            if (canal == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el canal cuando se asigna un dispositivo");
            dispositivo=dispositivoService.asegurarDispositivoLegacy(owner, deviceId);
            if (repository.existsByDeviceIdAndCanalAndUsuario_Id(deviceId, canal, owner.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un sensor para ese dispositivo y canal");
            }
        }
        Sensor s=new Sensor(r.getNombre().trim(), r.getTipo().trim(), r.getUnidad().trim(), "INACTIVO", r.getValorActual());
        s.setDeviceId(deviceId); s.setCanal(canal); s.setUsuario(owner);
        s.setDeviceAlias(dispositivo == null ? null : dispositivo.getAlias());
        s.setUltimaLectura(null); s.setEstado("INACTIVO");
        return repository.save(s);
    }

    @Transactional
    public Sensor actualizar(Long id, SensorRequest r, Usuario owner) {
        Sensor s=buscar(id, owner);
        s.setNombre(r.getNombre().trim()); s.setTipo(r.getTipo().trim()); s.setUnidad(r.getUnidad().trim());
        String requestedId=normalize(r.getDeviceId());
        String requestedChannel=normalize(r.getCanal());
        if (requestedId != null) {
            if (requestedChannel == null) requestedChannel=s.getCanal();
            Dispositivo d=dispositivoService.asegurarDispositivoLegacy(owner, requestedId);
            if (repository.existsByDeviceIdAndCanalAndUsuario_Id(requestedId, requestedChannel, owner.getId())
                    && !(requestedId.equals(s.getDeviceId()) && requestedChannel.equals(s.getCanal()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un sensor para ese dispositivo y canal");
            }
            s.setDeviceId(requestedId); s.setCanal(requestedChannel); s.setDeviceAlias(d.getAlias());
        }
        if (r.getValorActual() != null) s.setValorActual(r.getValorActual());
        return repository.save(s);
    }

    @Transactional
    public void eliminar(Long id, Usuario owner) {
        Sensor sensor=buscar(id, owner);
        graficoRepository.deleteBySensorId(id);
        lecturaRepository.deleteBySensorId(id);
        repository.delete(sensor);
    }

    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
