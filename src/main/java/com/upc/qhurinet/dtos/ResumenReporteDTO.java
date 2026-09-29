package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-41: indicadores del periodo
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenReporteDTO {
    private BigDecimal kgTotales;
    private Long totalRecojos;
    private BigDecimal calificacionPromedio;
    private BigDecimal cumplimiento;
}
