package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// END-24: nueva fecha coordinada
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReprogramarSolicitudDTO {
    private LocalDateTime fechaCoordinada;
    private String franjaHoraria;

}
