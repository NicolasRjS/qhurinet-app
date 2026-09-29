package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-09: metodo de pago preferido (tarjeta, yape, plin, transferencia, efectivo)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDTO {
    private String metodoPagoPreferido;
}
