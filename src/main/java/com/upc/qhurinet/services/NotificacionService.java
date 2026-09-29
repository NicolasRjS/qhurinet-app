package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.NotificacionDTO;
import com.upc.qhurinet.dtos.NotificacionesNoLeidasDTO;
import com.upc.qhurinet.entities.Usuario;

import java.util.List;

public interface NotificacionService {
    public List<NotificacionDTO> listar(String email);                     // END-50
    public NotificacionesNoLeidasDTO contarNoLeidas(String email);          // END-51
    public NotificacionDTO marcarComoLeida(String email, Long id);          // END-52

    // Usado por los demas servicios en cada cambio de estado de una recoleccion (US 15-EP2)
    public void notificar(Usuario usuario, String mensaje);
}
