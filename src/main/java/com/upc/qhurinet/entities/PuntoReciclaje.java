package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "puntos_reciclaje")
public class PuntoReciclaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String nombre;

    // acopio, bodega, reciclador, municipal; se valida en el servicio
    @Column(length = 20, nullable = false)
    private String tipo;

    @Column(length = 255, nullable = false)
    private String direccion;

    @Column(length = 80)
    private String distrito;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal longitud;

    @Column(length = 120)
    private String horarioAtencion;

    @Column(precision = 3, scale = 2)
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    // Tabla punto_reciclaje_materiales. Con Set la PK queda (punto_reciclaje_id, categoria_material_id).
    // Borrado en cascada del modelo (punto_reciclaje_materiales -> puntos_reciclaje), a nivel de
    // aplicacion: al eliminar el punto se eliminan sus filas en la tabla intermedia, no las categorias.
    @ManyToMany
    @JoinTable(name = "punto_reciclaje_materiales",
            joinColumns = @JoinColumn(name = "punto_reciclaje_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_material_id"))
    private Set<CategoriaMaterial> materiales = new HashSet<>();
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

}
