package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.NotificacionDTO;
import com.upc.qhurinet.dtos.NotificacionesNoLeidasDTO;
import com.upc.qhurinet.entities.Notificacion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.NotificacionRepositorio;
import com.upc.qhurinet.services.NotificacionService;
import com.upc.qhurinet.services.UsuarioService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotificacionServiceImpl implements NotificacionService {
    @Autowired
    private NotificacionRepositorio notificacionRepositorio;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ModelMapper modelMapper;

    // Se usa la coleccion Usuario.notificaciones; si se prefiere, reemplazar por una consulta ordenada
    @Override
    public List<NotificacionDTO> listar(String email) {
        return usuarioService.obtenerUsuario(email).getNotificaciones()
                .stream()
                .sorted(Comparator.comparing(Notificacion::getFechaCreacion).reversed())
                .map(notificacion -> modelMapper.map(notificacion, NotificacionDTO.class))
                .toList();
    }

    @Override
    public NotificacionesNoLeidasDTO contarNoLeidas(String email) {
        long cantidad = usuarioService.obtenerUsuario(email).getNotificaciones()
                .stream()
                .filter(notificacion -> !notificacion.isLeida())
                .count();
        return new NotificacionesNoLeidasDTO(cantidad);
    }

    @Transactional
    @Override
    public NotificacionDTO marcarComoLeida(String email, Long id) {
        Notificacion notificacion = notificacionRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notificación no encontrada"));
        if (!notificacion.getUsuario().getEmail().equals(email)) {
            throw new AccessDeniedException("La notificación no pertenece al usuario");
        }
        notificacion.setLeida(true);
        return modelMapper.map(notificacionRepositorio.save(notificacion), NotificacionDTO.class);
    }

    @Transactional
    @Override
    public void notificar(Usuario usuario, String mensaje) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(mensaje);
        notificacionRepositorio.save(notificacion);
    }
}
