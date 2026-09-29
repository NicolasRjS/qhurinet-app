package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeRepositorio extends JpaRepository<Mensaje, Long> {
}
