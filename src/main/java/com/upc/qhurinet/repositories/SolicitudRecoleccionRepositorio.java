package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.SolicitudRecoleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudRecoleccionRepositorio extends JpaRepository<SolicitudRecoleccion, Long> {
    List<SolicitudRecoleccion> findByRecolector_IdOrderByPrioritariaDescFechaCoordinadaAsc(Long recolectorId);
    List<SolicitudRecoleccion> findByRecolector_IdAndEstado(Long recolectorId, String estado);
    List<SolicitudRecoleccion> findByPublicacion_Generador_IdAndEstado(Long generadorId, String estado);

    @Query("select s from SolicitudRecoleccion s where s.recolector.id = :recolectorId "
            + "and s.estado = :estado and s.calificacionRecolector is not null")
    List<SolicitudRecoleccion> buscarCalificadasPorRecolector(@Param("recolectorId") Long recolectorId,
                                                            @Param("estado") String estado);
}
