package com.upc.qhurinet.security.filters;

import com.upc.qhurinet.security.services.CustomUserDetailsService;
import com.upc.qhurinet.security.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/*
    JwtRequestFilter se ejecuta una vez por cada solicitud: si trae un token JWT valido,
    autentica al usuario y lo deja en el contexto de seguridad. De ahi los controladores
    toman el email del usuario autenticado, nunca del cuerpo de la solicitud.
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRequestFilter(CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (JwtException | IllegalArgumentException e) {
                // Token invalido o expirado: la solicitud sigue sin autenticar y responde 401
            }
        }

        // Este es el punto clave donde se verifica si el token JWT es válido y se establece
        // la autenticación del usuario en el contexto de seguridad de Spring Security.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    //siempre por ser stateless, se debe establecer el contexto de seguridad
                }
            } catch (UsernameNotFoundException e) {
                // El usuario del token ya no existe: la solicitud sigue sin autenticar y responde 401
            }
        }
        chain.doFilter(request, response);//ya va al controller o al siguiente filtro en la cadena
    }
}
