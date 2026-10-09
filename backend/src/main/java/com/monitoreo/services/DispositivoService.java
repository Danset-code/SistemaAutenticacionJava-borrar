package com.monitoreo.services;

import com.monitoreo.dto.CodigoVinculacionResponse;
import com.monitoreo.dto.DispositivoResumen;
import com.monitoreo.dto.DispositivoVinculadoResponse;
import com.monitoreo.dto.VincularDispositivoRequest;
import com.monitoreo.models.Dispositivo;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.DispositivoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service
public class DispositivoService {
    private final DispositivoRepository repository;
    public DispositivoService(DispositivoRepository repository) { this.repository = repository; }

    @Transactional
    public CodigoVinculacionResponse crearCodigo(Usuario owner) {
        String alias = siguienteAlias(owner);
        Dispositivo d = new Dispositivo();
        d.setAlias(alias);
        d.setUsuario(owner);
        d.setApiKey(UUID.randomUUID().toString());
        d.setCodigoVinculacion(UUID.randomUUID().toString());
        repository.save(d);
        return new CodigoVinculacionResponse(alias, d.getCodigoVinculacion(),
                "Configure pairingCode en el microcontrolador. En el primer arranque, envíe su hardwareId y el código a POST /api/dispositivos/vincular; guarde el deviceKey devuelto en memoria persistente.");
    }

    @Transactional
    public DispositivoVinculadoResponse vincular(VincularDispositivoRequest request) {
        String hardwareId = request.getHardwareId() == null ? "" : request.getHardwareId().trim();
        String pairingCode = request.getPairingCode() == null ? "" : request.getPairingCode().trim();
        if (hardwareId.isBlank() || hardwareId.length() > 80 || pairingCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hardwareId y pairingCode son obligatorios");
        }
        if (repository.findByHardwareId(hardwareId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este hardwareId ya está vinculado a una cuenta");
        }
        Dispositivo d = repository.findByCodigoVinculacion(pairingCode).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código de vinculación inválido o ya utilizado"));
        if (d.getHardwareId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El código ya fue utilizado");
        }
        d.setHardwareId(hardwareId);
        d.setCodigoVinculacion(null); // código de un solo uso
        repository.save(d);
        return new DispositivoVinculadoResponse(true, d.getAlias(), hardwareId, d.getApiKey(),
                "Dispositivo vinculado. Guarde deviceKey de forma persistente y no lo publique.");
    }

    public List<DispositivoResumen> listar(Usuario owner) {
        return repository.findByUsuario_IdOrderByIdAsc(owner.getId()).stream().map(DispositivoResumen::new).toList();
    }

    public Dispositivo validarClave(String key) {
        if (key == null || key.isBlank()) return null;
        Dispositivo d = repository.findByApiKey(key.trim()).orElse(null);
        return d != null && d.getHardwareId() != null ? d : null;
    }

    @Transactional
    public Dispositivo asegurarDispositivoLegacy(Usuario owner, String hardwareId) {
        String id = hardwareId == null ? "" : hardwareId.trim();
        if (id.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El identificador físico es obligatorio");
        Dispositivo existing = repository.findByHardwareId(id).orElse(null);
        if (existing != null) {
            if (!existing.getUsuario().getId().equals(owner.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El dispositivo pertenece a otra cuenta");
            }
            return existing;
        }
        Dispositivo d = new Dispositivo();
        d.setHardwareId(id);
        d.setAlias(siguienteAlias(owner));
        d.setUsuario(owner);
        d.setApiKey(UUID.randomUUID().toString());
        return repository.save(d);
    }

    private String siguienteAlias(Usuario owner) {
        int n = 1;
        while (repository.existsByUsuario_IdAndAlias(owner.getId(), "device" + n)) n++;
        return "device" + n;
    }
}
