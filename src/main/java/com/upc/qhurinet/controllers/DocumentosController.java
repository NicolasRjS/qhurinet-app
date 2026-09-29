package com.upc.qhurinet.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Estructura HTTP inicial. TODO: conectar servicios y DTOs espec?ficos por endpoint. */
@RestController
@RequestMapping("/api/v1/users")
public class DocumentosController {
    // END-10: TODO conectar servicio y DTO de contrato.
    @PostMapping("/me/documents")
    public ResponseEntity<Void> end10() {
        return ResponseEntity.status(501).build();
    }

    // END-11: TODO conectar servicio y DTO de contrato.
    @GetMapping("/me/documents")
    public ResponseEntity<Void> end11() {
        return ResponseEntity.status(501).build();
    }

}
