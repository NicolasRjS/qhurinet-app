package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-30: calificacion de 1 a 5
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CalificarRecolectorDTO {
    private Integer calificacion;
    private String comentario;

}
