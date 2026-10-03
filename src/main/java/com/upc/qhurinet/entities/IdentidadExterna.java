package com.upc.qhurinet.entities;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "identidades_externas", uniqueConstraints =
        @UniqueConstraint(columnNames = {"proveedor", "identificador"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class IdentidadExterna {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false, length = 20)
    private String proveedor;
    @Column(nullable = false, length = 255)
    private String identificador;
}
