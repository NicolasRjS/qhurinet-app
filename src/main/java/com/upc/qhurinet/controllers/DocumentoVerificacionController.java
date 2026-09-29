package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.DocumentoVerificacionDTO;
import com.upc.qhurinet.services.DocumentoVerificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// Documentos de verificacion del usuario autenticado (END-10, END-11)
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/users")
public class DocumentoVerificacionController {
    @Autowired
    private DocumentoVerificacionService documentoVerificacionService;

    // END-10: multipart/form-data con los campos "archivo" y "tipo" (dni, ruc, otro)
    @PostMapping(value = "/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoVerificacionDTO> subir(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        DocumentoVerificacionDTO documento = documentoVerificacionService.subir(emailAutenticado(), tipo, archivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(documento);
    }

    // END-11
    @GetMapping("/me/documents")
    public ResponseEntity<List<DocumentoVerificacionDTO>> listarMisDocumentos() {
        return ResponseEntity.ok(documentoVerificacionService.listarMisDocumentos(emailAutenticado()));
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
