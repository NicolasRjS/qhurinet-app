package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-30: calificacion registrada y nuevo promedio del recolector
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalificacionDTO {

    private Integer calificacionRecolector;

    private BigDecimal calificacionPromedio;
}
