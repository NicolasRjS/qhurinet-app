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
@Table(name = "roles")
public class Rol { //No necesita conocer a los usuarios, por eso no es bidireccional

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // generador, recolector, administrador
    @Column(length = 40, nullable = false, unique = true)
    private String nombre;

    @Column(length = 150)
    private String descripcion;
}
