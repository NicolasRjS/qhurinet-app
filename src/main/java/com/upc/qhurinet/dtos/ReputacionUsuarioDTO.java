package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-12: reputacion publica del usuario (sin datos de contacto)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReputacionUsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String fotoPerfilUrl;
    private BigDecimal calificacionPromedio;
    private Long totalEntregas;
    private Boolean verificado;
}
