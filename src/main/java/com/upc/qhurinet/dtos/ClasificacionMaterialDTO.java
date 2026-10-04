package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-21: categoria sugerida por el servicio de clasificacion
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClasificacionMaterialDTO {

    private Integer categoriaMaterialId;

    private String categoriaMaterialNombre;

    private BigDecimal confianza;
}
