package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.TicketSoporte;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketSoporteRepositorio extends JpaRepository<TicketSoporte, Long> {

    List<TicketSoporte> findByUsuario_IdOrderByFechaCreacionDesc(Long usuarioId);

    List<TicketSoporte> findByUsuario_IdAndEstadoOrderByFechaCreacionDesc(
            Long usuarioId, String estado);

    Optional<TicketSoporte> findByEvidenciaUrl(String url);
}
