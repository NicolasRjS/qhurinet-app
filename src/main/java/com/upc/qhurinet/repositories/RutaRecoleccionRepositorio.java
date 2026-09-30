package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.RutaRecoleccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RutaRecoleccionRepositorio extends JpaRepository<RutaRecoleccion, Long> {
    List<RutaRecoleccion> findByRecolector_IdOrderByFechaCreacionDesc(Long recolectorId);
}
