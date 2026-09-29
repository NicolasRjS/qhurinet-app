package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.EnviarMensajeDTO;
import com.upc.qhurinet.dtos.MensajeDTO;
import com.upc.qhurinet.entities.Mensaje;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.repositories.MensajeRepositorio;
import com.upc.qhurinet.services.MensajeService;
import com.upc.qhurinet.services.SolicitudRecoleccionService;
import com.upc.qhurinet.services.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MensajeServiceImpl implements MensajeService {
    @Autowired
    private MensajeRepositorio mensajeRepositorio;
    @Autowired
    private SolicitudRecoleccionService solicitudRecoleccionService;
    @Autowired
    private UsuarioService usuarioService;

    // Solo las dos partes de la solicitud ven el chat. Usa la coleccion SolicitudRecoleccion.mensajes
    @Override
    public List<MensajeDTO> listar(String email, Long solicitudId) {
        SolicitudRecoleccion solicitud = solicitudRecoleccionService.obtenerSolicitudComoParte(email, solicitudId);
        return solicitud.getMensajes()
                .stream()
                .sorted(Comparator.comparing(Mensaje::getFechaEnvio))
                .map(this::aDTO)
                .toList();
    }

    // El remitente sale del token y debe ser una de las dos partes de la solicitud
    @Transactional
    @Override
    public MensajeDTO enviar(String email, Long solicitudId, EnviarMensajeDTO enviarMensajeDTO) {
        if (enviarMensajeDTO.getContenido() == null || enviarMensajeDTO.getContenido().isBlank()) {
            throw new IllegalArgumentException("contenido: es obligatorio");
        }
        SolicitudRecoleccion solicitud = solicitudRecoleccionService.obtenerSolicitudComoParte(email, solicitudId);
        Mensaje mensaje = new Mensaje();
        mensaje.setSolicitud(solicitud);
        mensaje.setRemitente(usuarioService.obtenerUsuario(email));
        mensaje.setContenido(enviarMensajeDTO.getContenido());
        return aDTO(mensajeRepositorio.save(mensaje));
    }

    // Mapeo manual: remitenteId es el unico dato del remitente que se expone
    private MensajeDTO aDTO(Mensaje mensaje) {
        return new MensajeDTO(mensaje.getId(), mensaje.getContenido(), mensaje.getFechaEnvio(),
                mensaje.getFechaLectura(), mensaje.getRemitente().getId());
    }
}
