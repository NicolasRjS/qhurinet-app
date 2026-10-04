package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// END-32, END-33: mensaje del chat de la solicitud
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MensajeDTO {

    private Long id;

    private String contenido;

    private LocalDateTime fechaEnvio;

    private LocalDateTime fechaLectura;

    private Long remitenteId;
}
