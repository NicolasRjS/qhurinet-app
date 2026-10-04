package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-28: datos de la entrega para que el recolector confirme visualmente
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EntregaQrDTO {

    private Long solicitudId;

    private String generador;

    private String direccion;

    private String material;

    private BigDecimal cantidad;
}
