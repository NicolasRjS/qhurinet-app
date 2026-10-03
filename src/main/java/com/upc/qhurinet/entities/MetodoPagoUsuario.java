package com.upc.qhurinet.entities;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name = "metodos_pago_usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MetodoPagoUsuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    @Column(nullable = false, length = 20)
    private String tipo;
    @Column(length = 34)
    private String dato;
    @Column(nullable = false)
    private boolean predeterminado;
}
