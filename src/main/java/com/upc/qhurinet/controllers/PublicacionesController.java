package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class PublicacionesController {
    // END-14: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GENERADOR')")
    @PostMapping("/publications")
    public ResponseEntity<Void> end14() {
        return ResponseEntity.status(501).build();
    }

    // END-15: TODO conectar servicio y DTO de contrato.
    @GetMapping("/publications/{id}")
    public ResponseEntity<Void> end15() {
        return ResponseEntity.status(501).build();
    }

    // END-16: TODO conectar servicio y DTO de contrato.
    @GetMapping("/publications")
    public ResponseEntity<Void> end16() {
        return ResponseEntity.status(501).build();
    }

    // END-17: TODO conectar servicio y DTO de contrato.
    @GetMapping("/publications/mine")
    public ResponseEntity<Void> end17() {
        return ResponseEntity.status(501).build();
    }

    // END-18: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GENERADOR')")
    @PutMapping("/publications/{id}")
    public ResponseEntity<Void> end18() {
        return ResponseEntity.status(501).build();
    }

    // END-19: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GENERADOR')")
    @PatchMapping("/publications/{id}/cancel")
    public ResponseEntity<Void> end19() {
        return ResponseEntity.status(501).build();
    }

    // END-20: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GENERADOR')")
    @PostMapping("/publications/{id}/photo")
    public ResponseEntity<Void> end20() {
        return ResponseEntity.status(501).build();
    }

    // END-21: TODO conectar servicio y DTO de contrato.
    @PostMapping("/materials/classify")
    public ResponseEntity<Void> end21() {
        return ResponseEntity.status(501).build();
    }

}
