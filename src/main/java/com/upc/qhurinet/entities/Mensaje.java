package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudRecoleccion solicitud;

    // Debe ser una de las 2 partes de la solicitud; se valida en el servicio
    @ManyToOne
    @JoinColumn(name = "remitente_id", nullable = false)
    private Usuario remitente;

    @Column(columnDefinition = "text")
    private String contenido;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    // null = no leido
    private LocalDateTime fechaLectura;
}
