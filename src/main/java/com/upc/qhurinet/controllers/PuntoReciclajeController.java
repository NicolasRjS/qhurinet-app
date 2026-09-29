package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.PuntoReciclajeDTO;
import com.upc.qhurinet.services.PuntoReciclajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Puntos de reciclaje del mapa (END-34, END-35)
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/recycling-points")
public class PuntoReciclajeController {
    @Autowired
    private PuntoReciclajeService puntoReciclajeService;

    // END-34: material admite varios ids (?material=1&material=2)
    @GetMapping
    public ResponseEntity<List<PuntoReciclajeDTO>> listar(
            @RequestParam(value = "material", required = false) List<Integer> material,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "q", required = false) String q) {
        return ResponseEntity.ok(puntoReciclajeService.listar(material, type, q));
    }

    // END-35
    @GetMapping("/{id}")
    public ResponseEntity<PuntoReciclajeDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(puntoReciclajeService.buscarPorId(id));
    }
}
