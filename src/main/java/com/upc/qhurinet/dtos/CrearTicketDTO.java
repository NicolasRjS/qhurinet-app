package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-46: datos del ticket. El usuario se toma del token; la evidencia se adjunta con END-48
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrearTicketDTO {

    private String categoria;

    private String asunto;

    private String descripcion;

    private Long solicitudId;
}
