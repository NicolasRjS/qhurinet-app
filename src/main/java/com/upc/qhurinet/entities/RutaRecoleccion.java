package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rutas_recoleccion")
public class RutaRecoleccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recolector_id", nullable = false)
    private Usuario recolector;

    @Column(length = 150, nullable = false)
    private String nombre;

    @Column(columnDefinition = "text")
    private String descripcion;

    // Opcional: una ruta guardada se reutiliza en jornadas futuras
    private LocalDate fechaRuta;

    @Column(precision = 6, scale = 2)
    private BigDecimal distanciaTotalKm;

    private Integer tiempoEstimadoMin;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Borrado en cascada del modelo (ruta_paradas -> rutas_recoleccion), a nivel de aplicacion.
    // Las paradas se guardan junto con la ruta (END-37) y se eliminan con ella (END-40)
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RutaParada> paradas = new ArrayList<>();
}
