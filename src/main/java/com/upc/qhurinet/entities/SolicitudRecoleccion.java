package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "solicitudes_recoleccion")
public class SolicitudRecoleccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "publicacion_id", nullable = false)
    private PublicacionMaterial publicacion;

    // Debe tener rol recolector; se valida en el servicio
    @ManyToOne
    @JoinColumn(name = "recolector_id")
    private Usuario recolector;

    // creada, coordinada, en_camino, ejecutada, cancelada; se valida en el servicio
    @Column(length = 20, nullable = false)
    private String estado = "creada";

    @Column(nullable = false)
    private boolean prioritaria = false;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    private LocalDateTime fechaCoordinada;

    private LocalDateTime fechaEjecucion;

    // Momento en que se valido el QR
    private LocalDateTime fechaValidacion;

    // Registra el motivo de cancelacion (US 09-EP2)
    @Column(columnDefinition = "text")
    private String observaciones;

    @Column(length = 100)
    private String codigoQr;

    @Column(nullable = false)
    private boolean qrValidado = false;

    // 1 a 5; solo si recolector_id no es null. Se valida en el servicio
    private Integer calificacionRecolector;

    // > 0 si no es null. Se valida en el servicio
    @Column(precision = 10, scale = 2)
    private BigDecimal montoPago;

    // tarjeta, yape, plin, transferencia, efectivo; se valida en el servicio
    @Column(length = 20)
    private String metodoPago;

    // Borrado en cascada del modelo (mensajes -> solicitudes_recoleccion), a nivel de aplicacion
    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajes = new ArrayList<>();
}
