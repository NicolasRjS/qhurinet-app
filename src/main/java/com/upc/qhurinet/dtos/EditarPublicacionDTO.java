package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// END-18: campos editables mientras la publicacion esta disponible
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EditarPublicacionDTO {
    private BigDecimal cantidad;
    private String descripcion;
    private String direccion;
    private LocalDate fechaDisponibilidad;
    private String franjaHoraria;
    private BigDecimal montoPago;
    private String metodoPago;

    private Integer categoriaMaterialId;
    private String unidadMedida;
    private String distrito;
    private BigDecimal latitud;
    private BigDecimal longitud;

}
