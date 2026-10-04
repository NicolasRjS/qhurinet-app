package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// END-22, END-24, END-25, END-26, END-29: solicitud de recoleccion. No expone el codigo QR
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDTO {

    private Long id;

    private Long publicacionId;

    private Long recolectorId;

    private String estado;

    private Boolean prioritaria;

    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaCoordinada;

    private LocalDateTime fechaEjecucion;

    private LocalDateTime fechaValidacion;

    private String observaciones;

    private Boolean qrValidado;

    private Integer calificacionRecolector;

    private BigDecimal montoPago;

    private String metodoPago;

    private String franjaHoraria;

    private String comentarioCalificacion;
}
