package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-38: ruta del listado con su numero de paradas
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RutaResumenDTO {

    private Long id;

    private String nombre;

    private String descripcion;

    private BigDecimal distanciaTotalKm;

    private Integer tiempoEstimadoMin;

    private Long totalParadas;
}
