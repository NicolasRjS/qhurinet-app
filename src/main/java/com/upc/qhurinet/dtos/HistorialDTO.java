package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// END-43: entrega o recojo del historial con su valoracion
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HistorialDTO {
    private LocalDateTime fecha;
    private String material;
    private BigDecimal cantidad;
    private String contraparte;
    private Integer calificacionRecolector;
    private String estado;
    private BigDecimal montoPago;
}
