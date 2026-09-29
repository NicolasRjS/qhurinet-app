package com.upc.qhurinet.security.services;

import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.UsuarioRepositorio;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
/**
 * Busca al usuario en la BD por su email (el "username" de la cuenta).
 * Si no existe -> lanza excepcion.
 * Convierte su rol en GrantedAuthority con el prefijo ROLE_ y en mayusculas:
 * generador -> ROLE_GENERADOR, recolector -> ROLE_RECOLECTOR, administrador -> ROLE_ADMINISTRADOR.
 * Asi los controladores usan @PreAuthorize("hasRole('GENERADOR')").
 * Es usado por JwtRequestFilter y por el AuthenticationManager en el login.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public CustomUserDetailsService(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Un usuario tiene un solo rol (los roles son excluyentes)
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre().toUpperCase()));

        return org.springframework.security.core.userdetails.User
                .withUsername(usuario.getEmail())
                .password(usuario.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
