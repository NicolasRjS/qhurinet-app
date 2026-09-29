package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-42: kilos y recolecciones por categoria de material
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMaterialDTO {
    private String material;
    private BigDecimal kilos;
    private Long recolecciones;
}
