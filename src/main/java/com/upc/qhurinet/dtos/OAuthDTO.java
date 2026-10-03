package com.upc.qhurinet.dtos;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OAuthDTO {
    private String idToken;
    private String accessToken;
    private Integer rolId;
    private String telefono;
    private String passwordActual;
}
