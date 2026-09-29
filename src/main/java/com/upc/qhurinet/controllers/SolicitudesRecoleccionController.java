package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class SolicitudesRecoleccionController {
    // END-22: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PostMapping("/collection-requests")
    public ResponseEntity<Void> end22() {
        return ResponseEntity.status(501).build();
    }

    // END-23: TODO conectar servicio y DTO de contrato.
    @GetMapping("/collection-requests")
    public ResponseEntity<Void> end23() {
        return ResponseEntity.status(501).build();
    }

    // END-24: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PatchMapping("/collection-requests/{id}/reschedule")
    public ResponseEntity<Void> end24() {
        return ResponseEntity.status(501).build();
    }

    // END-25: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PatchMapping("/collection-requests/{id}/cancel")
    public ResponseEntity<Void> end25() {
        return ResponseEntity.status(501).build();
    }

    // END-26: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PatchMapping("/collection-requests/{id}/priority")
    public ResponseEntity<Void> end26() {
        return ResponseEntity.status(501).build();
    }

    // END-27: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @GetMapping("/collection-requests/{id}/qr")
    public ResponseEntity<Void> end27() {
        return ResponseEntity.status(501).build();
    }

    // END-28: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PostMapping("/collection-requests/{id}/validate-qr")
    public ResponseEntity<Void> end28() {
        return ResponseEntity.status(501).build();
    }

    // END-29: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PostMapping("/collection-requests/{id}/confirm")
    public ResponseEntity<Void> end29() {
        return ResponseEntity.status(501).build();
    }

    // END-30: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @PostMapping("/collection-requests/{id}/rating")
    public ResponseEntity<Void> end30() {
        return ResponseEntity.status(501).build();
    }

    // END-31: TODO conectar servicio y DTO de contrato.
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('RECOLECTOR')")
    @GetMapping("/collection-requests/{id}/tracking")
    public ResponseEntity<Void> end31() {
        return ResponseEntity.status(501).build();
    }

}
