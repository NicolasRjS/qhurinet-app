package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// END-34, END-35: punto de reciclaje con sus materiales aceptados
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PuntoReciclajeDTO {
    private Long id;
    private String nombre;
    private String tipo;
    private String direccion;
    private String distrito;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private String horarioAtencion;
    private BigDecimal calificacionPromedio;
    private List<CategoriaMaterialDTO> materiales;
}
