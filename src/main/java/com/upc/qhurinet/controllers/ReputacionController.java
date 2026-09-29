package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1/users")
public class ReputacionController {
    // END-12: TODO conectar servicio y DTO de contrato.
    @GetMapping("/{id}/reputation")
    public ResponseEntity<Void> end12() {
        return ResponseEntity.status(501).build();
    }

}
