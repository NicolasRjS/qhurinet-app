package com.upc.qhurinet.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuarioDTO {

    private Long id;

    private String tipo;

    private String dato;

    private Boolean predeterminado;
}
