package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-33: contenido del mensaje. El remitente se toma del token
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnviarMensajeDTO {

    private String contenido;
}
