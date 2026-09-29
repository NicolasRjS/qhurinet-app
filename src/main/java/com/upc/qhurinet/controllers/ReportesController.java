package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class ReportesController {
    // END-41: TODO conectar servicio y DTO de contrato.
    @GetMapping("/reports/summary")
    public ResponseEntity<Void> end41() {
        return ResponseEntity.status(501).build();
    }

    // END-42: TODO conectar servicio y DTO de contrato.
    @GetMapping("/reports/materials")
    public ResponseEntity<Void> end42() {
        return ResponseEntity.status(501).build();
    }

    // END-43: TODO conectar servicio y DTO de contrato.
    @GetMapping("/history")
    public ResponseEntity<Void> end43() {
        return ResponseEntity.status(501).build();
    }

    // END-44: TODO conectar servicio y DTO de contrato.
    @GetMapping("/reports/export")
    public ResponseEntity<Void> end44() {
        return ResponseEntity.status(501).build();
    }

}
