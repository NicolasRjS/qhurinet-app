package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.*;
import org.springframework.web.multipart.MultipartFile;

// El parametro email siempre viene del token (usuario autenticado), nunca del cuerpo
public interface UsuarioService {
    public UsuarioDTO registrar(RegistroUsuarioDTO registroUsuarioDTO);                         // END-01
    public UsuarioDTO buscarPorEmail(String email);                                             // END-02
    public UsuarioDTO verificarCorreo(String email);                                            // END-04
    public PerfilUsuarioDTO obtenerPerfil(String email);                                        // END-05
    public PerfilUsuarioDTO actualizarPerfil(String email, ActualizarPerfilDTO actualizarPerfilDTO); // END-06
    public FotoPerfilDTO actualizarFotoPerfil(String email, MultipartFile archivo);             // END-07
    public DisponibilidadDTO actualizarDisponibilidad(String email, DisponibilidadDTO disponibilidadDTO); // END-08
    public MetodoPagoDTO actualizarMetodoPago(String email, MetodoPagoDTO metodoPagoDTO);      // END-09
}
