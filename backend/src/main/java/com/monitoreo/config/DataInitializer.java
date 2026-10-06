package com.monitoreo.config;

import com.monitoreo.models.TipoSensor;
import com.monitoreo.models.Usuario;
import com.monitoreo.repositories.TipoSensorRepository;
import com.monitoreo.repositories.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner init(
            UsuarioRepository usuarioRepository,
            TipoSensorRepository tipoSensorRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Crear administrador solamente si no existe
            if (!usuarioRepository.existsByCorreo("admin@monitoreo.com")) {

                Usuario admin = new Usuario();

                admin.setNombre("Administrador");
                admin.setCorreo("admin@monitoreo.com");
                admin.setPassword(
                        passwordEncoder.encode("Admin123")
                );

                usuarioRepository.save(admin);
            }
        };
    }
}