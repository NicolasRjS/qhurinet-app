package com.upc.qhurinet.repositories;

import com.upc.qhurinet.entities.PublicacionMaterial;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublicacionMaterialRepositorio extends JpaRepository<PublicacionMaterial, Long> {
    List<PublicacionMaterial> findByEstado(String estado);
    List<PublicacionMaterial> findByGenerador_IdOrderByFechaPublicacionDesc(Long generadorId);

    @Query("select s from SolicitudRecoleccion s where s.publicacion.generador.id = :generadorId " +
            "and s.estado <> 'cancelada' order by s.fechaSolicitud desc")
    List<SolicitudRecoleccion> buscarSolicitudesVigentesPorGenerador(@Param("generadorId") Long generadorId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select e from PublicacionMaterial e where e.id = :id")
    java.util.Optional<PublicacionMaterial> buscarParaActualizar(@org.springframework.data.repository.query.Param("id") Long id);

    java.util.List<PublicacionMaterial> findByFotoUrl(String url);

}
