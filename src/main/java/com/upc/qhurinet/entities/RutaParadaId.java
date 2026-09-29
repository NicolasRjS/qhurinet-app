package com.upc.qhurinet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// Clave compuesta de ruta_paradas: (ruta_id, punto_reciclaje_id)
// @Data genera equals y hashCode, que JPA exige en una clave compuesta
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RutaParadaId implements Serializable {

    @Column(name = "ruta_id")
    private Long rutaId;

    @Column(name = "punto_reciclaje_id")
    private Long puntoReciclajeId;
}
