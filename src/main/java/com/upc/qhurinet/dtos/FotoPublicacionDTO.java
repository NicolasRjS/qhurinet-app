package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-20: ruta donde quedo guardada la foto de la publicacion
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FotoPublicacionDTO {
    private String fotoUrl;
}
