package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// END-17: publicacion del generador con su reciclador y fecha coordinada
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MiPublicacionDTO {

    private Long id;

    private BigDecimal cantidad;

    private String material;

    private LocalDateTime fechaPublicacion;

    private String estado;

    private String reciclador;

    private LocalDateTime fechaCoordinada;

    private Long solicitudId;

    private String franjaHoraria;

    private String unidadMedida;

    public MiPublicacionDTO(
            Long id,
            BigDecimal cantidad,
            String material,
            LocalDateTime fechaPublicacion,
            String estado,
            String reciclador,
            LocalDateTime fechaCoordinada) {
        this.id = id;
        this.cantidad = cantidad;
        this.material = material;
        this.fechaPublicacion = fechaPublicacion;
        this.estado = estado;
        this.reciclador = reciclador;
        this.fechaCoordinada = fechaCoordinada;
    }
}
