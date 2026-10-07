import com.monitoreo.models.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {

    // Tus métodos actuales...

    List<Sensor> findByUltimaLecturaBeforeAndEstadoNot(
            LocalDateTime fecha,
            String estado
    );

    // Si ya tienes este método, no lo dupliques:
    Optional<Sensor> findByDeviceIdAndCanal(String deviceId, String canal);

}
