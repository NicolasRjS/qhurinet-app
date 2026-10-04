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
        // El motivo puede contener datos personales: no copiarlos al aviso (US 15).
        mensaje = mensaje.replaceAll("[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}", "[correo omitido]")
                .replaceAll("(?<!\\d)(?:\\+?\\d[ ().-]*){9,15}(?!\\d)", "[teléfono omitido]");
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje(mensaje.length() > 255 ? mensaje.substring(0, 252) + "..." : mensaje);
        notificacionRepositorio.save(notificacion);
    }

    // PATCH /notifications/read-all: solo las propias notificaciones pendientes
    @Override
    @Transactional
    public void marcarTodas(String email) {
        usuarioService.obtenerUsuario(email).getNotificaciones()
                .stream()
                .filter(notificacion -> !notificacion.isLeida())
                .forEach(notificacion -> {
                    notificacion.setLeida(true);
                    notificacionRepositorio.save(notificacion);
                });
    }

}
