package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.services.PublicacionMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Publicaciones de material (END-14 a END-20). Solo el rol generador publica y modifica
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/publications")
public class PublicacionMaterialController {
    @Autowired
    private PublicacionMaterialService publicacionMaterialService;

    // END-14
    @PostMapping
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<PublicacionDTO> crear(@RequestBody CrearPublicacionDTO crearPublicacionDTO) {
        PublicacionDTO publicacion = publicacionMaterialService.crear(emailAutenticado(), crearPublicacionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacion);
    }

    // END-16: filtros del mapa. fecha en formato yyyy-MM-dd
    @GetMapping
    public ResponseEntity<List<PublicacionDTO>> listarParaMapa(
            @RequestParam(value = "material", required = false) Integer material,
            @RequestParam(value = "distrito", required = false) String distrito,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "min_kg", required = false) BigDecimal minKg,
            @RequestParam(value = "fecha", required = false) LocalDate fecha,
            @RequestParam(value = "estado", required = false) String estado) {
        return ResponseEntity.ok(publicacionMaterialService.listarParaMapa(material, distrito, q, minKg, fecha, estado));
    }

    // END-17: estado = activos | completados | cancelados
    @GetMapping("/mine")
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<List<MiPublicacionDTO>> listarMisPublicaciones(
            @RequestParam(value = "estado", required = false) String estado) {
        return ResponseEntity.ok(publicacionMaterialService.listarMisPublicaciones(emailAutenticado(), estado));
    }

    // END-15
    @GetMapping("/{id}")
    public ResponseEntity<PublicacionDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(publicacionMaterialService.buscarPorId(id));
    }

    // END-18
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<PublicacionDTO> editar(@PathVariable Long id, @RequestBody EditarPublicacionDTO editarPublicacionDTO) {
        return ResponseEntity.ok(publicacionMaterialService.editar(emailAutenticado(), id, editarPublicacionDTO));
    }

    // END-19
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<PublicacionDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(publicacionMaterialService.cancelar(emailAutenticado(), id));
    }

    // END-20: multipart/form-data con el campo "archivo"
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('GENERADOR')")
    public ResponseEntity<FotoPublicacionDTO> subirFoto(@PathVariable Long id,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        return ResponseEntity.ok(publicacionMaterialService.subirFoto(emailAutenticado(), id, archivo));
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
