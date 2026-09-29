package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// END-31: seguimiento de la recoleccion en camino
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoDTO {
    private Long solicitudId;
    private String estado;
    private LocalDateTime fechaCoordinada;
    private String recolector;
    private String destino;
    private BigDecimal latitudDestino;
    private BigDecimal longitudDestino;
    private BigDecimal latitudActual;
    private BigDecimal longitudActual;
    private Integer minutosEstimados;
}
