package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-36, END-37, END-39: parada de una ruta. Al guardar solo se envian puntoReciclajeId y orden
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParadaRutaDTO {
    private Long puntoReciclajeId;
    private Integer orden;
    private String nombre;
    private String direccion;
    private BigDecimal latitud;
    private BigDecimal longitud;
}
