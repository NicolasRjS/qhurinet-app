package com.upc.qhurinet.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-04: token de confirmacion enviado al correo
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificarCorreoDTO {
    private String token;
}
