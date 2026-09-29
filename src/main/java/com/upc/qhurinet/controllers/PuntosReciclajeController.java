package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class PuntosReciclajeController {
    // END-34: TODO conectar servicio y DTO de contrato.
    @GetMapping("/recycling-points")
    public ResponseEntity<Void> end34() {
        return ResponseEntity.status(501).build();
    }

    // END-35: TODO conectar servicio y DTO de contrato.
    @GetMapping("/recycling-points/{id}")
    public ResponseEntity<Void> end35() {
        return ResponseEntity.status(501).build();
    }

}
