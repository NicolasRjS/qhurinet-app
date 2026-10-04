package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// END-14, END-15, END-16, END-18, END-19: publicacion de material
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicacionDTO {

    private Long id;

    private Integer categoriaMaterialId;

    private String categoriaMaterialNombre;

    private BigDecimal cantidad;

    private String unidadMedida;

    private String descripcion;

    private String estado;

    private String fotoUrl;

    private String direccion;

    private String distrito;

    private BigDecimal latitud;

    private BigDecimal longitud;

    private LocalDate fechaDisponibilidad;

    private LocalDateTime fechaPublicacion;

    private Long generadorId;

    private String generadorNombreCompleto;

    private String franjaHoraria;

    private BigDecimal montoPago;

    private String metodoPago;
}
