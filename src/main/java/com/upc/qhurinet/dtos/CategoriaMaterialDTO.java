package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// END-13: categoria del catalogo con su unidad por defecto
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaMaterialDTO {

    private Integer id;

    private String nombre;

    private String unidadMedidaDefault;
}
