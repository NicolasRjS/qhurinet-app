package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.PublicacionMaterial;
import com.upc.qhurinet.entities.SolicitudRecoleccion;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublicacionMaterialRepositorio extends JpaRepository<PublicacionMaterial, Long> {

    List<PublicacionMaterial> findByEstado(String estado);

    List<PublicacionMaterial> findByGenerador_IdOrderByFechaPublicacionDesc(Long generadorId);

    @Query(
            "select s from SolicitudRecoleccion s where s.publicacion.generador.id = :generadorId "
                    + "and s.estado <> 'cancelada' order by s.fechaSolicitud desc")
    List<SolicitudRecoleccion> buscarSolicitudesVigentesPorGenerador(
            @Param("generadorId") Long generadorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from PublicacionMaterial e where e.id = :id")
    Optional<PublicacionMaterial> buscarParaActualizar(@Param("id") Long id);

    List<PublicacionMaterial> findByFotoUrl(String url);
}
