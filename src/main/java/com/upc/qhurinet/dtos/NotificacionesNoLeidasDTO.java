package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-51: cantidad de notificaciones sin leer
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionesNoLeidasDTO {

    private Long cantidad;
}
