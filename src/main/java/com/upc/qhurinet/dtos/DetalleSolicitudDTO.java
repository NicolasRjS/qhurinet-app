package com.upc.qhurinet.dtos;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleSolicitudDTO {

    private SolicitudDTO solicitud;

    private Long generadorId;

    private Long recolectorId;

    private String contraparte;

    private String material;

    private BigDecimal cantidad;

    private String unidadMedida;

    private String direccion;
}
