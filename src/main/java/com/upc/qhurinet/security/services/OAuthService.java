package com.upc.qhurinet.security.services;

import com.upc.qhurinet.config.ClaveFederadaConfig;
import com.upc.qhurinet.dtos.OAuthDTO;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.UsuarioRepositorio;
import com.upc.qhurinet.security.util.JwtUtil;
import com.upc.qhurinet.services.CorreoService;
import com.upc.qhurinet.services.RolService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OAuthService {

    @Autowired
    private ClaveFederadaConfig claveFederadaConfig;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private RolService rolService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CorreoService correoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${oauth.google.client-id:}")
    private String googleClientId;

    @Value("${oauth.google.jwks:https://www.googleapis.com/oauth2/v3/certs}")
    private String googleJwks;

    @Value("${oauth.facebook.app-id:}")
    private String facebookAppId;

    @Value("${oauth.facebook.app-secret:}")
    private String facebookSecret;

    @Value("${oauth.facebook.url:https://graph.facebook.com}")
    private String facebookUrl;

    @Value("${oauth.facebook.version:v23.0}")
    private String facebookVersion;

    private final RestClient cliente = crearCliente();

    private volatile JwtDecoder googleDecoder;

    @Transactional
    public Usuario autenticar(String proveedor, OAuthDTO datos) {
        if (!List.of("google", "facebook").contains(proveedor)) {
            throw new IllegalArgumentException("provider: google o facebook");
        }
        Identidad identidad =
                "google".equals(proveedor)
                        ? google(datos.getIdToken())
                        : facebook(datos.getAccessToken());
        var vinculada =
                usuarioRepositorio.findByProveedorExternoAndIdExterno(proveedor, identidad.id);
        if (vinculada.isPresent()) {
            return vinculada.get();
        }
        if (identidad.email == null
                || !identidad.email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
                || identidad.email.length() > 150) {
            throw new IllegalArgumentException(
                    "email: el proveedor debe compartir un correo válido");
        }
        String email = identidad.email.toLowerCase(Locale.ROOT).trim();
        Usuario usuario = usuarioRepositorio.findByEmail(email).orElse(null);
        if (usuario != null) {
            usuario = usuarioRepositorio.buscarParaActualizar(usuario.getId()).orElseThrow();
            // No confiar en un correo de terceros no verificado para asociar una cuenta existente.
            if (!identidad.correoConfiable
                    && (usuario.getPasswordHash() == null
                            || datos.getPasswordActual() == null
                            || !passwordEncoder.matches(
                                    datos.getPasswordActual(), usuario.getPasswordHash()))) {
                throw new BadCredentialsException("Se requiere verificar la cuenta local");
            }
        } else {
            if (datos.getRolId() == null
                    || datos.getTelefono() == null
                    || !datos.getTelefono().matches("\\d{9}")) {
                throw new IllegalArgumentException(
                        "rolId y telefono: obligatorios en el primer acceso; teléfono de 9"
                            + " dígitos");
            }
            if (identidad.nombre == null
                    || identidad.nombre.isBlank()
                    || identidad.nombre.length() > 150) {
                throw new IllegalArgumentException(
                        "nombreCompleto: el proveedor no suministró un nombre válido");
            }
            usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setNombreCompleto(identidad.nombre);
            usuario.setPasswordHash(claveFederadaConfig.generarHash());
            usuario.setTelefono(datos.getTelefono());
            usuario.setRol(rolService.buscarRolDeRegistro(datos.getRolId()));
            usuario.setEstado(identidad.correoConfiable ? "activo" : "pendiente_verificacion");
            usuario = usuarioRepositorio.saveAndFlush(usuario);
            if (!identidad.correoConfiable) {
                correoService.enviarVerificacion(email, jwtUtil.generarTokenVerificacion(email));
            }
        }
        if (identidad.correoConfiable) {
            usuario.setEstado("activo");
        }
        // Una cuenta conserva una sola identidad externa; no sustituir una asociacion previa.
        if (usuario.getProveedorExterno() == null) {
            usuario.setProveedorExterno(proveedor);
            usuario.setIdExterno(identidad.id);
        }
        usuarioRepositorio.save(usuario);
        return usuario;
    }

    private RestClient crearCliente() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(5));
        f.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(f).build();
    }

    private Identidad google(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("idToken: obligatorio para Google");
        }
        if (googleClientId.isBlank()) {
            throw new RestClientException("Google no configurado");
        }
        if (googleDecoder == null) {
            synchronized (this) {
                if (googleDecoder == null) {
                    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
                    f.setConnectTimeout(Duration.ofSeconds(5));
                    f.setReadTimeout(Duration.ofSeconds(10));
                    NimbusJwtDecoder decoder =
                            NimbusJwtDecoder.withJwkSetUri(googleJwks)
                                    .restOperations(new RestTemplate(f))
                                    .build();
                    decoder.setJwtValidator(JwtValidators.createDefault());
                    googleDecoder = decoder;
                }
            }
        }
        try {
            Jwt tokenValido = googleDecoder.decode(token);
            String issuer = tokenValido.getClaimAsString("iss");
            if (!List.of("accounts.google.com", "https://accounts.google.com").contains(issuer)
                    || !tokenValido.getAudience().contains(googleClientId)
                    || tokenValido.getExpiresAt() == null
                    || tokenValido.getSubject() == null
                    || tokenValido.getSubject().isBlank()) {
                throw new BadCredentialsException("Token inválido");
            }
            String email = tokenValido.getClaimAsString("email");
            boolean verificado =
                    Boolean.TRUE.equals(tokenValido.getClaimAsBoolean("email_verified"));
            boolean autoridad =
                    email != null
                            && (email.toLowerCase(Locale.ROOT).endsWith("@gmail.com")
                                    || tokenValido.hasClaim("hd"));
            return new Identidad(
                    tokenValido.getSubject(),
                    email,
                    tokenValido.getClaimAsString("name"),
                    verificado && autoridad);
        } catch (BadJwtException ex) {
            throw new BadCredentialsException("Token inválido");
        } catch (JwtException ex) {
            throw new RestClientException("No se pudo verificar el proveedor");
        }
    }

    private Identidad facebook(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("accessToken: obligatorio para Facebook");
        }
        if (facebookAppId.isBlank() || facebookSecret.isBlank()) {
            throw new RestClientException("Facebook no configurado");
        }
        try {
            Map<?, ?> debug =
                    cliente.get()
                            .uri(
                                    facebookUrl
                                            + "/"
                                            + facebookVersion
                                            + "/debug_token?input_token={token}",
                                    token)
                            .header(
                                    "Authorization",
                                    "Bearer " + facebookAppId + "|" + facebookSecret)
                            .retrieve()
                            .body(Map.class);
            if (debug == null
                    || !(debug.get("data") instanceof Map<?, ?> data)
                    || !Boolean.TRUE.equals(data.get("is_valid"))
                    || !facebookAppId.equals(data.get("app_id"))
                    || !(data.get("expires_at") instanceof Number expires)
                    || expires.longValue() <= Instant.now().getEpochSecond()
                    || !(data.get("user_id") instanceof String id)
                    || id.isBlank()) {
                throw new BadCredentialsException("Token inválido");
            }
            Map<?, ?> perfil =
                    cliente.get()
                            .uri(facebookUrl + "/" + facebookVersion + "/me?fields=id,name,email")
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .body(Map.class);
            if (perfil == null || !id.equals(perfil.get("id"))) {
                throw new BadCredentialsException("Token inválido");
            }
            return new Identidad(
                    id, (String) perfil.get("email"), (String) perfil.get("name"), false);
        } catch (HttpClientErrorException ex) {
            throw new BadCredentialsException("Token inválido");
        }
    }

    private static class Identidad {

        String id, email, nombre;

        boolean correoConfiable;

        Identidad(String id, String email, String nombre, boolean confiable) {
            this.id = id;
            this.email = email;
            this.nombre = nombre;
            this.correoConfiable = confiable;
        }
    }
}
