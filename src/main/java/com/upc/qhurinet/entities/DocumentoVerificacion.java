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
@Table(name = "documentos_verificacion")
public class DocumentoVerificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // dni, ruc, otro; se valida en el servicio
    @Column(length = 10, nullable = false)
    private String tipo;

    // El archivo vive en el servicio de almacenamiento; la base guarda la URL
    @Column(length = 255, nullable = false)
    private String urlArchivo;

    // aprobado, rechazado; validacion automatica al subir (US 27-EP4)
    @Column(length = 20, nullable = false)
    private String estado = "aprobado";

    @Column(nullable = false)
    private LocalDateTime fechaSubida = LocalDateTime.now();
}
