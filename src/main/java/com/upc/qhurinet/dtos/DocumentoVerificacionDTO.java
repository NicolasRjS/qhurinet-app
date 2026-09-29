package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// END-10, END-11: documento de verificacion del usuario
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoVerificacionDTO {
    private Long id;
    private String tipo;
    private String urlArchivo;
    private String estado;
    private LocalDateTime fechaSubida;
}
