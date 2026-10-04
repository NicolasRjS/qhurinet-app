package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-22: publicacion que reclama el recolector. El recolector se toma del token
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrearSolicitudDTO {

    private Long publicacionId;
}
