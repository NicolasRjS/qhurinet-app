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
@Table(name = "tickets_soporte")
public class TicketSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    private SolicitudRecoleccion solicitud;

    // problema_con_recolector, problema_con_generador, material_no_coincide,
    // error_en_plataforma, otro. Cubre US 12-EP2 (incidencias) y US 39-EP6 (reclamos)
    @Column(length = 30, nullable = false)
    private String categoria;

    // abierto, cerrado; se valida en el servicio
    @Column(length = 10, nullable = false)
    private String estado = "abierto";

    @Column(length = 150, nullable = false)
    private String asunto;

    @Column(columnDefinition = "text", nullable = false)
    private String descripcion;

    @Column(length = 255)
    private String evidenciaUrl;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private LocalDateTime fechaCierre;
}
