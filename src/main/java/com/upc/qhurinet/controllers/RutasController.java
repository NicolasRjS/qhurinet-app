package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class RutasController {
    // END-36: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PostMapping("/routes/optimize")
    public ResponseEntity<Void> end36() {
        return ResponseEntity.status(501).build();
    }

    // END-37: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PostMapping("/routes")
    public ResponseEntity<Void> end37() {
        return ResponseEntity.status(501).build();
    }

    // END-38: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @GetMapping("/routes")
    public ResponseEntity<Void> end38() {
        return ResponseEntity.status(501).build();
    }

    // END-39: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @GetMapping("/routes/{id}")
    public ResponseEntity<Void> end39() {
        return ResponseEntity.status(501).build();
    }

    // END-40: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> end40() {
        return ResponseEntity.status(501).build();
    }

}
