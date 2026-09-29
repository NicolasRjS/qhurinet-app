package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.services.RutaRecoleccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Rutas del recolector (END-36 a END-40)
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/routes")
@PreAuthorize("hasRole('RECOLECTOR')")
public class RutaRecoleccionController {
    @Autowired
    private RutaRecoleccionService rutaRecoleccionService;

    // END-36
    @PostMapping("/optimize")
    public ResponseEntity<RutaOptimizadaDTO> optimizar(@RequestBody OptimizarRutaDTO optimizarRutaDTO) {
        return ResponseEntity.ok(rutaRecoleccionService.optimizar(emailAutenticado(), optimizarRutaDTO));
    }

    // END-37
    @PostMapping
    public ResponseEntity<RutaDTO> crear(@RequestBody CrearRutaDTO crearRutaDTO) {
        RutaDTO ruta = rutaRecoleccionService.crear(emailAutenticado(), crearRutaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ruta);
    }

    // END-38
    @GetMapping
    public ResponseEntity<List<RutaResumenDTO>> listarMisRutas() {
        return ResponseEntity.ok(rutaRecoleccionService.listarMisRutas(emailAutenticado()));
    }

    // END-39
    @GetMapping("/{id}")
    public ResponseEntity<RutaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(rutaRecoleccionService.buscarPorId(emailAutenticado(), id));
    }

    // END-40: 204 sin contenido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rutaRecoleccionService.eliminar(emailAutenticado(), id);
        return ResponseEntity.noContent().build();
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
