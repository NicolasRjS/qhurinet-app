package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.CategoriaMaterialDTO;
import com.upc.qhurinet.dtos.ClasificacionMaterialDTO;
import com.upc.qhurinet.services.CategoriaMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Catalogo de materiales (END-13) y clasificacion automatica (END-21)
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1")
public class CategoriaMaterialController {
    @Autowired
    private CategoriaMaterialService categoriaMaterialService;

    // END-13
    @GetMapping("/material-categories")
    public ResponseEntity<List<CategoriaMaterialDTO>> listar() {
        return ResponseEntity.ok(categoriaMaterialService.listar());
    }

    // END-21: multipart/form-data con "foto" o "descripcion"
    @PostMapping(value = "/materials/classify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<ClasificacionMaterialDTO> clasificar(
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "descripcion", required = false) String descripcion) {
        return ResponseEntity.ok(categoriaMaterialService.clasificar(foto, descripcion));
    }
}
