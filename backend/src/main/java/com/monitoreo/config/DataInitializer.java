package com.monitoreo.config;

import com.monitoreo.models.Grafico;
import com.monitoreo.models.Lectura;
import com.monitoreo.models.Sensor;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.GraficoRepository;
import com.monitoreo.repositories.LecturaRepository;
import com.monitoreo.repositories.SensorRepository;
import com.monitoreo.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            UsuarioRepository usuarioRepository,
            SensorRepository sensorRepository,
            GraficoRepository graficoRepository,
            LecturaRepository lecturaRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            if (!usuarioRepository.existsByCorreo("admin@monitoreo.com")) {
                Usuario admin = new Usuario();
                admin.setNombre("Administrador");
                admin.setCorreo("admin@monitoreo.com");
                admin.setPassword(passwordEncoder.encode("Admin123"));
                usuarioRepository.save(admin);
            }

            if (sensorRepository.count() == 0) {
                Sensor temperatura = sensorRepository.save(
                        new Sensor("Sensor Temperatura", "Temperatura", "°C", "ACTIVO", 25.0));
                Sensor humedad = sensorRepository.save(
                        new Sensor("Sensor Humedad", "Humedad", "%", "ACTIVO", 70.0));
                Sensor co2 = sensorRepository.save(
                        new Sensor("Sensor CO₂", "CO₂", "ppm", "ACTIVO", 420.0));

                if (graficoRepository.count() == 0) {
                    graficoRepository.save(new Grafico("Temperatura ambiente", "LINEAL", temperatura, true));
                    graficoRepository.save(new Grafico("Humedad del cultivo", "AREA", humedad, true));
                    graficoRepository.save(new Grafico("CO₂ ambiental", "BARRAS", co2, true));
                }

                Random random = new Random(7);
                LocalDateTime base = LocalDateTime.now().minusHours(2);

                for (int i = 0; i < 24; i++) {
                    lecturaRepository.save(new Lectura(temperatura,
                            24.0 + random.nextDouble() * 5.0,
                            base.plusMinutes(i * 5)));
                    lecturaRepository.save(new Lectura(humedad,
                            62.0 + random.nextDouble() * 18.0,
                            base.plusMinutes(i * 5)));
                    lecturaRepository.save(new Lectura(co2,
                            380.0 + random.nextDouble() * 100.0,
                            base.plusMinutes(i * 5)));
                }
            }
        };
    }
}
