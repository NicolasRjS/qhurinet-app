package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class NotificacionesController {
    // END-50: TODO conectar servicio y DTO de contrato.
    @GetMapping("/notifications")
    public ResponseEntity<Void> end50() {
        return ResponseEntity.status(501).build();
    }

    // END-51: TODO conectar servicio y DTO de contrato.
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Void> end51() {
        return ResponseEntity.status(501).build();
    }

    // END-52: TODO conectar servicio y DTO de contrato.
    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<Void> end52() {
        return ResponseEntity.status(501).build();
    }

}
