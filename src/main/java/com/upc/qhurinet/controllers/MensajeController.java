package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.EnviarMensajeDTO;
import com.upc.qhurinet.dtos.MensajeDTO;
import com.upc.qhurinet.services.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Chat de una solicitud de recoleccion (END-32, END-33). Solo sus dos partes
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/collection-requests")
public class MensajeController {
    @Autowired
    private MensajeService mensajeService;

    // END-32
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MensajeDTO>> listar(@PathVariable Long id) {
        return ResponseEntity.ok(mensajeService.listar(emailAutenticado(), id));
    }

    // END-33
    @PostMapping("/{id}/messages")
    public ResponseEntity<MensajeDTO> enviar(@PathVariable Long id, @RequestBody EnviarMensajeDTO enviarMensajeDTO) {
        MensajeDTO mensaje = mensajeService.enviar(emailAutenticado(), id, enviarMensajeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
