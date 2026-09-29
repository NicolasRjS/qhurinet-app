package com.upc.qhurinet.security.dtos;

import com.upc.qhurinet.dtos.UsuarioDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-02: token de sesion junto con los datos basicos del usuario
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private UsuarioDTO usuario;
}
