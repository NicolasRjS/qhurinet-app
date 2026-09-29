package com.upc.qhurinet.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// END-50, END-52: notificacion del usuario
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {
    private Long id;
    private String mensaje;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
}
