package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ruta_paradas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ruta_id", "orden"}))
public class RutaParada {

    @EmbeddedId
    private RutaParadaId id = new RutaParadaId();

    // @MapsId toma el valor de la clave compuesta desde la relacion
    @ManyToOne
    @MapsId("rutaId")
    @JoinColumn(name = "ruta_id")
    private RutaRecoleccion ruta;

    @ManyToOne
    @MapsId("puntoReciclajeId")
    @JoinColumn(name = "punto_reciclaje_id")
    private PuntoReciclaje puntoReciclaje;

    @Column(nullable = false)
    private Integer orden;
}
