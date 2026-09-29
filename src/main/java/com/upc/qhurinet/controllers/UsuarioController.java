package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
 Perfil del usuario autenticado (END-05 a END-09) y reputacion publica (END-12).
 Todas las rutas son /me: el usuario se identifica con el token, nunca con un id del cuerpo o de la ruta.
 Cualquier rol autenticado puede usarlas (SecurityConfig: anyRequest().authenticated()).
 Para restringir por rol en otros controladores: @PreAuthorize("hasRole('GENERADOR')").
*/
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1/users")
public class UsuarioController {
    @Autowired
    private UsuarioService usuarioService;

    // END-05
    @GetMapping("/me")
    public ResponseEntity<PerfilUsuarioDTO> obtenerPerfil() {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(emailAutenticado()));
    }

    // END-06
    @PutMapping("/me")
    public ResponseEntity<PerfilUsuarioDTO> actualizarPerfil(@RequestBody ActualizarPerfilDTO actualizarPerfilDTO) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(emailAutenticado(), actualizarPerfilDTO));
    }

    // END-07: multipart/form-data con el campo "archivo"
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FotoPerfilDTO> actualizarFotoPerfil(
            @RequestParam(value = "archivo", required = false) MultipartFile archivo) {
        return ResponseEntity.ok(usuarioService.actualizarFotoPerfil(emailAutenticado(), archivo));
    }

    // END-08
    @PatchMapping("/me/availability")
    public ResponseEntity<DisponibilidadDTO> actualizarDisponibilidad(@RequestBody DisponibilidadDTO disponibilidadDTO) {
        return ResponseEntity.ok(usuarioService.actualizarDisponibilidad(emailAutenticado(), disponibilidadDTO));
    }

    // END-09
    @PatchMapping("/me/payment-method")
    public ResponseEntity<MetodoPagoDTO> actualizarMetodoPago(@RequestBody MetodoPagoDTO metodoPagoDTO) {
        return ResponseEntity.ok(usuarioService.actualizarMetodoPago(emailAutenticado(), metodoPagoDTO));
    }

    // END-12: reputacion publica de otro usuario (generador o recolector)
    @GetMapping("/{id}/reputation")
    public ResponseEntity<ReputacionUsuarioDTO> obtenerReputacion(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerReputacion(id));
    }

    // El JwtRequestFilter dejo al usuario en el contexto; su nombre es el email (sujeto del token)
    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
