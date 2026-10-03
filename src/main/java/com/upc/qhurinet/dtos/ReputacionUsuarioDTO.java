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
    private String descripcion;
    private String rolNombre;
    private java.util.List<CategoriaMaterialDTO> materiales;

    public ReputacionUsuarioDTO(Long id, String nombreCompleto, String fotoPerfilUrl, BigDecimal calificacionPromedio, Long totalEntregas, Boolean verificado) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.calificacionPromedio = calificacionPromedio;
        this.totalEntregas = totalEntregas;
        this.verificado = verificado;
    }

}
