package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-06: campos editables del perfil. El email no es editable (US 26-EP4)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilDTO {
    private String nombreCompleto;
    private String telefono;
    private String descripcion;
    private java.util.List<Integer> materialesIds;

}
