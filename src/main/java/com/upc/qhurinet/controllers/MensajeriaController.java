package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class MensajeriaController {
    // END-32: TODO conectar servicio y DTO de contrato.
    @GetMapping("/collection-requests/{id}/messages")
    public ResponseEntity<Void> end32() {
        return ResponseEntity.status(501).build();
    }

    // END-33: TODO conectar servicio y DTO de contrato.
    @PostMapping("/collection-requests/{id}/messages")
    public ResponseEntity<Void> end33() {
        return ResponseEntity.status(501).build();
    }

}
