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
    public HistorialDTO(LocalDateTime fecha, String material, BigDecimal cantidad, String contraparte, Integer calificacionRecolector, String estado, BigDecimal montoPago) {
        this.fecha = fecha;
        this.material = material;
        this.cantidad = cantidad;
        this.contraparte = contraparte;
        this.calificacionRecolector = calificacionRecolector;
        this.estado = estado;
        this.montoPago = montoPago;
    }

    private Long solicitudId;
    private String unidadMedida;

}
