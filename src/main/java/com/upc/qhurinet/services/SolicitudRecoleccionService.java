package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.CalificacionDTO;
import com.upc.qhurinet.dtos.CalificarRecolectorDTO;
import com.upc.qhurinet.dtos.CancelarSolicitudDTO;
import com.upc.qhurinet.dtos.CodigoQrDTO;
import com.upc.qhurinet.dtos.CrearSolicitudDTO;
import com.upc.qhurinet.dtos.DetalleSolicitudDTO;
import com.upc.qhurinet.dtos.EntregaQrDTO;
import com.upc.qhurinet.dtos.MiSolicitudDTO;
import com.upc.qhurinet.dtos.PrioridadSolicitudDTO;
import com.upc.qhurinet.dtos.ReprogramarSolicitudDTO;
import com.upc.qhurinet.dtos.SeguimientoDTO;
import com.upc.qhurinet.dtos.SolicitudDTO;
import com.upc.qhurinet.dtos.UbicacionDTO;
import com.upc.qhurinet.dtos.ValidarQrDTO;
import com.upc.qhurinet.entities.SolicitudRecoleccion;

import java.util.List;

public interface SolicitudRecoleccionService {
    public SolicitudDTO crear(String email, CrearSolicitudDTO crearSolicitudDTO);                               // END-22
    public List<MiSolicitudDTO> listarMisSolicitudes(String email, String estado);                              // END-23
    public SolicitudDTO reprogramar(String email, Long id, ReprogramarSolicitudDTO reprogramarSolicitudDTO);   // END-24
    public SolicitudDTO cancelar(String email, Long id, CancelarSolicitudDTO cancelarSolicitudDTO);            // END-25
    public SolicitudDTO cambiarPrioridad(String email, Long id, PrioridadSolicitudDTO prioridadSolicitudDTO);  // END-26
    public CodigoQrDTO obtenerCodigoQr(String email, Long id);                                                  // END-27
    public EntregaQrDTO validarQr(String email, Long id, ValidarQrDTO validarQrDTO);                           // END-28
    public SolicitudDTO confirmarEntrega(String email, Long id, ValidarQrDTO datos);                           // END-29
    public CalificacionDTO calificar(String email, Long id, CalificarRecolectorDTO calificarRecolectorDTO);    // END-30
    public SeguimientoDTO obtenerSeguimiento(String email, Long id);                                            // END-31
    public SolicitudDTO coordinar(String email, Long id, ReprogramarSolicitudDTO datos);                       // PATCH /{id}/coordinate
    public SolicitudDTO iniciar(String email, Long id);                                                         // PATCH /{id}/start
    public void actualizarUbicacion(String email, Long id, UbicacionDTO datos);                                 // PUT /{id}/location
    public DetalleSolicitudDTO obtenerDetalle(String email, Long id);                                           // GET /{id}

    // Usado por MensajeService: la solicitud si el usuario es una de sus dos partes (404 / 403)
    public SolicitudRecoleccion obtenerSolicitudComoParte(String email, Long id);
}
