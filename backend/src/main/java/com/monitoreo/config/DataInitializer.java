package com.monitoreo.config;

import com.monitoreo.models.Dispositivo;
import com.monitoreo.models.Sensor;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.DispositivoRepository;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.UUID;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner init(UsuarioRepository usuarios, PasswordEncoder encoder,
                           SensorRepository sensores, DispositivoRepository dispositivos) {
        return args -> {
            Usuario admin=usuarios.findByCorreo("admin@monitoreo.com").orElseGet(() -> {
                Usuario u=new Usuario(); u.setNombre("Administrador"); u.setCorreo("admin@monitoreo.com");
                u.setPassword(encoder.encode("Admin123")); return usuarios.save(u);
            });

            // Migración de datos anteriores: los registros sin propietario se conservan bajo el administrador.
            List<Sensor> lista=sensores.findAll();
            for (Sensor sensor : lista) {
                if (sensor.getUsuario() == null) sensor.setUsuario(admin);
            }
            sensores.saveAll(lista);

            // Registra IDs físicos que ya existían y prepara el alias device1, device2... automáticamente.
            for (Sensor sensor : sensores.findAll()) {
                if (sensor.getUsuario() == null || sensor.getDeviceId() == null || sensor.getDeviceId().isBlank()) continue;
                Dispositivo d=dispositivos.findByHardwareId(sensor.getDeviceId()).orElse(null);
                if (d == null) {
                    d=new Dispositivo(); d.setHardwareId(sensor.getDeviceId()); d.setUsuario(sensor.getUsuario());
                    d.setAlias(siguienteAlias(dispositivos, sensor.getUsuario()));
                    d.setApiKey(UUID.randomUUID().toString());
                    d=dispositivos.save(d);
                }
                if (d.getUsuario().getId().equals(sensor.getUsuario().getId())
                        && (sensor.getDeviceAlias() == null || sensor.getDeviceAlias().isBlank())) {
                    sensor.setDeviceAlias(d.getAlias()); sensores.save(sensor);
                }
            }
        };
    }
    private String siguienteAlias(DispositivoRepository repo, Usuario owner) {
        int n=1; while (repo.existsByUsuario_IdAndAlias(owner.getId(), "device"+n)) n++;
        return "device"+n;
    }
}
