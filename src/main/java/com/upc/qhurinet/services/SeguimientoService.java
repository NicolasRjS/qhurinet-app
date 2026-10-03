package com.upc.qhurinet.services;
import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
public interface SeguimientoService {
    void actualizar(Long id, UbicacionDTO ubicacion);
    SeguimientoDTO obtener(SolicitudRecoleccion solicitud);
    void eliminar(Long id);
}
