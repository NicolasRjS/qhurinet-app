package com.upc.qhurinet.dtos;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {

    private BigDecimal latitud;

    private BigDecimal longitud;
}
