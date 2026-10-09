package com.monitoreo.repositories;

import com.monitoreo.models.UsuarioSesion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioSesionRepository extends JpaRepository<UsuarioSesion, String> {
    void deleteByUsuario_Id(Long usuarioId);
}
