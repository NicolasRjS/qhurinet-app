package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-01: datos que envia el cliente para registrarse
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarUsuarioDTO {
    private String nombreCompleto;
    private String email;
    private String password;
    private String telefono;
    private Integer rolId; // generador o recolector
}
