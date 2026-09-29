package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// END-36: origen del recolector y puntos seleccionados (2 a 10)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptimizarRutaDTO {
    private BigDecimal latitudOrigen;
    private BigDecimal longitudOrigen;
    private List<Long> puntosIds;
}
