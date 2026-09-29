package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-49: telefono y horario de atencion de soporte
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContactoSoporteDTO {
    private String telefono;
    private String horarioAtencion;
}
