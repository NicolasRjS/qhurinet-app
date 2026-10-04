package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.SolicitudRecoleccion;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SolicitudRecoleccionRepositorio extends JpaRepository<SolicitudRecoleccion, Long> {

    List<SolicitudRecoleccion> findByRecolector_IdOrderByPrioritariaDescFechaCoordinadaAsc(
            Long recolectorId);

    List<SolicitudRecoleccion> findByRecolector_IdAndEstado(Long recolectorId, String estado);

    List<SolicitudRecoleccion> findByRecolector_IdAndEstadoIn(
            Long recolectorId, List<String> estados);

    List<SolicitudRecoleccion> findByPublicacion_Generador_IdAndEstado(
            Long generadorId, String estado);

    List<SolicitudRecoleccion> findByPublicacion_Generador_IdAndEstadoIn(
            Long generadorId, List<String> estados);

    @Query(
            "select s from SolicitudRecoleccion s where s.recolector.id = :recolectorId "
                    + "and s.estado = :estado and s.calificacionRecolector is not null")
    List<SolicitudRecoleccion> buscarCalificadasPorRecolector(
            @Param("recolectorId") Long recolectorId,
            @Param("estado") String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from SolicitudRecoleccion e where e.id = :id")
    Optional<SolicitudRecoleccion> buscarParaActualizar(@Param("id") Long id);
}
