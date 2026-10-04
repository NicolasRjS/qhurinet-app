package com.upc.qhurinet.security.controllers;

import com.upc.qhurinet.dtos.OAuthDTO;
import com.upc.qhurinet.dtos.RegistrarUsuarioDTO;
import com.upc.qhurinet.dtos.UsuarioDTO;
import com.upc.qhurinet.security.dtos.AuthRequestDTO;
import com.upc.qhurinet.security.dtos.AuthResponseDTO;
import com.upc.qhurinet.security.dtos.VerificarCorreoDTO;
import com.upc.qhurinet.security.services.CustomUserDetailsService;
import com.upc.qhurinet.security.services.OAuthService;
import com.upc.qhurinet.security.util.JwtUtil;
import com.upc.qhurinet.services.UsuarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(
        origins = "${ip.frontend}",
        allowCredentials = "true",
        exposedHeaders = "Authorization")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    private final UsuarioService usuarioService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.usuarioService = usuarioService;
    }

    @Autowired
    private OAuthService oauthService;

    // END-03
    @PostMapping("/oauth/{provider}")
    public ResponseEntity<AuthResponseDTO> oauth(
            @PathVariable String provider,
            @RequestBody OAuthDTO datos) {
        var usuario = oauthService.autenticar(provider, datos);
        String token =
                jwtUtil.generateToken(userDetailsService.loadUserByUsername(usuario.getEmail()));
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(
                        new AuthResponseDTO(
                                token, usuarioService.buscarPorEmail(usuario.getEmail())));
    }

    // END-01: 201 con la cuenta en estado pendiente_verificacion
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> registrar(
            @RequestBody RegistrarUsuarioDTO registrarUsuarioDTO) {
        UsuarioDTO usuarioDTO = usuarioService.registrar(registrarUsuarioDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioDTO);
    }

    // END-02: 200 con el token y los datos basicos; 401 si las credenciales no coinciden
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequest) {
        String email =
                authRequest.getEmail() == null ? "" : authRequest.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, authRequest.getPassword()));
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        final String token = jwtUtil.generateToken(userDetails);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Authorization", token);
        AuthResponseDTO authResponseDTO =
                new AuthResponseDTO(token, usuarioService.buscarPorEmail(email));
        return ResponseEntity.ok().headers(responseHeaders).body(authResponseDTO);
    }

    // END-04: activa la cuenta con el token de confirmacion; 409 si ya estaba activa
    @PostMapping("/verify-email")
    public ResponseEntity<UsuarioDTO> verificarCorreo(
            @RequestBody VerificarCorreoDTO verificarCorreoDTO) {
        String email = jwtUtil.extraerEmailDeVerificacion(verificarCorreoDTO.getToken());
        return ResponseEntity.ok(usuarioService.verificarCorreo(email));
    }
}
