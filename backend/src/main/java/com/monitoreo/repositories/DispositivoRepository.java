package com.monitoreo.repositories;

import com.monitoreo.models.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    Optional<Dispositivo> findByHardwareId(String hardwareId);
    Optional<Dispositivo> findByCodigoVinculacion(String codigoVinculacion);
    Optional<Dispositivo> findByApiKey(String apiKey);
    List<Dispositivo> findByUsuario_IdOrderByIdAsc(Long usuarioId);
    boolean existsByUsuario_IdAndAlias(Long usuarioId, String alias);
    boolean existsByHardwareId(String hardwareId);
    void deleteByUsuario_Id(Long usuarioId);
}
