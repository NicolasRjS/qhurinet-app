package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-01, END-02, END-04: datos basicos de la cuenta. Nunca incluye el password
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private Integer rolId;
    private String rolNombre;
    private String estado;
}
