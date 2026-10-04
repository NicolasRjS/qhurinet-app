package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-25: motivo de la cancelacion (obligatorio)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelarSolicitudDTO {

    private String motivo;
}
