package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// END-05, END-06: perfil completo del usuario autenticado
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUsuarioDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String fotoPerfilUrl;
    private String descripcion;
    private String metodoPagoPreferido;
    private Boolean enLinea;
    private BigDecimal calificacionPromedio;
    private String estado;
    private Integer rolId;
    private String rolNombre;
    private java.util.List<CategoriaMaterialDTO> materiales;
    private java.util.List<MetodoPagoUsuarioDTO> metodosPago;
    private Boolean verificado;

}
