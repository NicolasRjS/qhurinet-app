package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-08: disponibilidad para recibir solicitudes (columna en_linea)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilidadDTO {

    private Boolean enLinea;
}
