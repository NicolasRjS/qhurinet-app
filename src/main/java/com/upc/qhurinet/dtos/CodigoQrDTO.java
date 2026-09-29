package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-27: codigo QR que muestra el generador
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CodigoQrDTO {
    private String codigoQr;
    private Boolean qrValidado;
}
