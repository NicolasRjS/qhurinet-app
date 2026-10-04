package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-45: pregunta frecuente
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FaqDTO {

    private String pregunta;

    private String respuesta;
}
