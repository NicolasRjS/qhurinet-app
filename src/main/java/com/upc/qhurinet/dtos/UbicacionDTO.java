package com.upc.qhurinet.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UbicacionDTO {
    private BigDecimal latitud;
    private BigDecimal longitud;
}
