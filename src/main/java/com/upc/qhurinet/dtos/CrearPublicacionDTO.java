package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// END-14: datos del anuncio. El generador se toma del token
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrearPublicacionDTO {
    private Integer categoriaMaterialId;
    private BigDecimal cantidad;
    private String unidadMedida;
    private String descripcion;
    private String fotoUrl;
    private String direccion;
    private String distrito;
    private BigDecimal latitud;
    private BigDecimal longitud;
    private LocalDate fechaDisponibilidad;
    private String franjaHoraria;
    private BigDecimal montoPago;
    private String metodoPago;

}
