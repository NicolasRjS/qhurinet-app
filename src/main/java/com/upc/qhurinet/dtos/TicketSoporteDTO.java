package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// END-46, END-47: ticket de soporte
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TicketSoporteDTO {

    private Long id;

    private Long solicitudId;

    private String categoria;

    private String estado;

    private String asunto;

    private String descripcion;

    private String evidenciaUrl;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaCierre;
}
