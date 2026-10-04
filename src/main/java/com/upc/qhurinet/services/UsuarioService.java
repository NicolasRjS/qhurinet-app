package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.ActualizarPerfilDTO;
import com.upc.qhurinet.dtos.DisponibilidadDTO;
import com.upc.qhurinet.dtos.FotoPerfilDTO;
import com.upc.qhurinet.dtos.MetodoPagoDTO;
import com.upc.qhurinet.dtos.PerfilUsuarioDTO;
import com.upc.qhurinet.dtos.RegistrarUsuarioDTO;
import com.upc.qhurinet.dtos.ReputacionUsuarioDTO;
import com.upc.qhurinet.dtos.UsuarioDTO;
import com.upc.qhurinet.entities.Usuario;
import org.springframework.web.multipart.MultipartFile;

// El parametro email siempre viene del token (usuario autenticado), nunca del cuerpo
public interface UsuarioService {
    public UsuarioDTO registrar(RegistrarUsuarioDTO registrarUsuarioDTO);                         // END-01
    public UsuarioDTO buscarPorEmail(String email);                                             // END-02
    public UsuarioDTO verificarCorreo(String email);                                            // END-04
    public PerfilUsuarioDTO obtenerPerfil(String email);                                        // END-05
    public PerfilUsuarioDTO actualizarPerfil(String email, ActualizarPerfilDTO actualizarPerfilDTO); // END-06
    public FotoPerfilDTO actualizarFotoPerfil(String email, MultipartFile archivo);             // END-07
    public DisponibilidadDTO actualizarDisponibilidad(String email, DisponibilidadDTO disponibilidadDTO); // END-08
    public MetodoPagoDTO actualizarMetodoPago(String email, MetodoPagoDTO metodoPagoDTO);      // END-09
    public ReputacionUsuarioDTO obtenerReputacion(Long id);                                     // END-12

    // Usado por los demas servicios para cargar al usuario autenticado (404 si ya no existe)
    public Usuario obtenerUsuario(String email);
}
