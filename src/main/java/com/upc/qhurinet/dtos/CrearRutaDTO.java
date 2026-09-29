package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// END-37: ruta a guardar con sus paradas ordenadas. El recolector se toma del token
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrearRutaDTO {
    private String nombre;
    private String descripcion;
    private LocalDate fechaRuta;
    private BigDecimal distanciaTotalKm;
    private Integer tiempoEstimadoMin;
    private List<ParadaRutaDTO> paradas;
}
