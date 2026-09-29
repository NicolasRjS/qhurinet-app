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
@Table(name = "categorias_material")
public class CategoriaMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // carton, PET, vidrio, metal, plastico
    @Column(length = 60, nullable = false, unique = true)
    private String nombre;

    @Column(length = 10, nullable = false)
    private String unidadMedidaDefault = "kg";
}
