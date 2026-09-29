package com.upc.qhurinet.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

//Clase que se encargara de generar y validar los tokens JWT.
@Component
public class JwtUtil {

    // Hay dos tipos de token firmados con la misma clave:
    // - sesion: autentica las solicitudes (END-02)
    // - verificacion_correo: solo sirve para activar la cuenta (END-04)
    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_SESION = "sesion";
    private static final String TIPO_VERIFICACION = "verificacion_correo";

    private static final long DURACION_SESION = 1000 * 60 * 20; // 20 min
    private static final long DURACION_VERIFICACION = 1000 * 60 * 60 * 24; // 24 horas

    @Value("${jwt.secret}")
    private String secretKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // El sujeto del token es el email del usuario
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // LLENANDO EL PAYLOAD
        claims.put(CLAIM_TIPO, TIPO_SESION);
        claims.put("rol", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null));
        return createToken(claims, userDetails.getUsername(), DURACION_SESION);
    }

    public String generarTokenVerificacion(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TIPO, TIPO_VERIFICACION);
        return createToken(claims, email, DURACION_VERIFICACION);
    }

    private String createToken(Map<String, Object> claims, String subject, long duracion) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + duracion))
                .signWith(SignatureAlgorithm.HS512, secretKey).compact();
    }

    // Solo acepta tokens de sesion: un token de verificacion no autentica solicitudes
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        final String tipo = extractClaim(token, claims -> claims.get(CLAIM_TIPO, String.class));
        return (username.equals(userDetails.getUsername()) && TIPO_SESION.equals(tipo) && !isTokenExpired(token));
    }

    // Devuelve el email del token de verificacion; 400 si es invalido, expiro o no es de verificacion
    public String extraerEmailDeVerificacion(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (!TIPO_VERIFICACION.equals(claims.get(CLAIM_TIPO, String.class))) {
                throw new IllegalArgumentException("token: no es un token de verificación de correo");
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("token: el token de verificación no es válido o expiró");
        }
    }
}
