package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1")
public class MaterialesController {
    // END-13: TODO conectar servicio y DTO de contrato.
    @GetMapping("/material-categories")
    public ResponseEntity<Void> end13() {
        return ResponseEntity.status(501).build();
    }

}
