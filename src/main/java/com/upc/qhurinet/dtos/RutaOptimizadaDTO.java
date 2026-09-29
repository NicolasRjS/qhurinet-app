package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

// END-36: orden de visita calculado por el servicio externo
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RutaOptimizadaDTO {
    private List<ParadaRutaDTO> paradas;
    private BigDecimal distanciaTotalKm;
    private Integer tiempoEstimadoMin;
}
