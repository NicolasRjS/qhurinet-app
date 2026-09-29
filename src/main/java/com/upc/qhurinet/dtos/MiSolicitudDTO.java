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
}
