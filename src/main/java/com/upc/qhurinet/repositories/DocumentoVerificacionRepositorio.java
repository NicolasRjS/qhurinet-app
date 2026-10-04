package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.DocumentoVerificacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoVerificacionRepositorio
        extends JpaRepository<DocumentoVerificacion, Long> {

    List<DocumentoVerificacion> findByUsuario_IdOrderByFechaSubidaDesc(Long usuarioId);

    List<DocumentoVerificacion> findByUsuario_IdAndEstado(Long usuarioId, String estado);

    Optional<DocumentoVerificacion> findByUrlArchivo(String url);
}
