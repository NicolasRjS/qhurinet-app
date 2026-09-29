package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// END-37, END-39: ruta guardada con sus paradas en orden de visita
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RutaDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaRuta;
    private BigDecimal distanciaTotalKm;
    private Integer tiempoEstimadoMin;
    private LocalDateTime fechaCreacion;
    private List<ParadaRutaDTO> paradas;
}
