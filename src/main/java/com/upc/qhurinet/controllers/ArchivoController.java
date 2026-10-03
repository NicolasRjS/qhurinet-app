package com.upc.qhurinet.controllers;
import com.upc.qhurinet.services.AlmacenamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/files")
public class ArchivoController {
    @Autowired private AlmacenamientoService almacenamientoService;
    @GetMapping("/{id}")
    public ResponseEntity<Resource> obtener(@PathVariable String id) {
        Resource recurso = almacenamientoService.obtener(SecurityContextHolder.getContext().getAuthentication().getName(), id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(almacenamientoService.tipo(id.substring(id.lastIndexOf('.') + 1))))
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + id + "\"").body(recurso);
    }
}
