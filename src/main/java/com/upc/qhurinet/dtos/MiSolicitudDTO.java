package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// END-23: recojo del usuario con su contraparte y ventana horaria
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MiSolicitudDTO {
    private Long id;
    private BigDecimal cantidad;
    private String material;
    private String direccion;
    private String estado;
    private Boolean prioritaria;
    private String contraparte;
    private LocalDateTime fechaCoordinada;
    public MiSolicitudDTO(Long id, BigDecimal cantidad, String material, String direccion, String estado, Boolean prioritaria, String contraparte, LocalDateTime fechaCoordinada) {
        this.id = id;
        this.cantidad = cantidad;
        this.material = material;
        this.direccion = direccion;
        this.estado = estado;
        this.prioritaria = prioritaria;
        this.contraparte = contraparte;
        this.fechaCoordinada = fechaCoordinada;
    }

    private Long publicacionId;
    private String franjaHoraria;
    private String unidadMedida;

}
