package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.NotificacionDTO;
import com.upc.qhurinet.dtos.NotificacionesNoLeidasDTO;
import com.upc.qhurinet.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Notificaciones del usuario autenticado (END-50 a END-52)
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/notifications")
public class NotificacionController {
    @Autowired
    private NotificacionService notificacionService;

    // END-50
    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> listar() {
        return ResponseEntity.ok(notificacionService.listar(emailAutenticado()));
    }

    // END-51
    @GetMapping("/unread-count")
    public ResponseEntity<NotificacionesNoLeidasDTO> contarNoLeidas() {
        return ResponseEntity.ok(notificacionService.contarNoLeidas(emailAutenticado()));
    }

    // END-52
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(emailAutenticado(), id));
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
