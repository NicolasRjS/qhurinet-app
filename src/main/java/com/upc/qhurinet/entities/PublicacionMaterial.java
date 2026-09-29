package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Un material por publicacion: carton y vidrio son dos publicaciones independientes
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "publicaciones_material",
        indexes = @Index(name = "idx_publicaciones_mapa", columnList = "estado, distrito"))
public class PublicacionMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Debe tener rol generador; se valida en el servicio
    @ManyToOne
    @JoinColumn(name = "generador_id", nullable = false)
    private Usuario generador;

    @ManyToOne
    @JoinColumn(name = "categoria_material_id", nullable = false)
    private CategoriaMaterial categoriaMaterial;

    // CHECK cantidad > 0; se valida en el servicio
    @Column(precision = 8, scale = 2, nullable = false)
    private BigDecimal cantidad;

    // Por defecto toma unidad_medida_default de la categoria
    @Column(length = 10, nullable = false)
    private String unidadMedida = "kg";

    // Limite de 200 caracteres validado en la aplicacion (US 02-EP1)
    @Column(columnDefinition = "text")
    private String descripcion;

    // disponible, reservado, recolectado, cancelado; se valida en el servicio
    @Column(length = 20, nullable = false)
    private String estado = "disponible";

    @Column(length = 255)
    private String fotoUrl;

    @Column(length = 255, nullable = false)
    private String direccion;

    @Column(length = 80)
    private String distrito;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 7, nullable = false)
    private BigDecimal longitud;

    private LocalDate fechaDisponibilidad;

    @Column(nullable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();
}
