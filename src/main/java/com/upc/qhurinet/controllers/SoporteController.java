package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class SoporteController {
    // END-45: TODO conectar servicio y DTO de contrato.
    @GetMapping("/faqs")
    public ResponseEntity<Void> end45() {
        return ResponseEntity.status(501).build();
    }

    // END-46: TODO conectar servicio y DTO de contrato.
    @PostMapping("/support-tickets")
    public ResponseEntity<Void> end46() {
        return ResponseEntity.status(501).build();
    }

    // END-47: TODO conectar servicio y DTO de contrato.
    @GetMapping("/support-tickets")
    public ResponseEntity<Void> end47() {
        return ResponseEntity.status(501).build();
    }

    // END-48: TODO conectar servicio y DTO de contrato.
    @PostMapping("/support-tickets/{id}/evidence")
    public ResponseEntity<Void> end48() {
        return ResponseEntity.status(501).build();
    }

    // END-49: TODO conectar servicio y DTO de contrato.
    @GetMapping("/support/contact")
    public ResponseEntity<Void> end49() {
        return ResponseEntity.status(501).build();
    }

}
