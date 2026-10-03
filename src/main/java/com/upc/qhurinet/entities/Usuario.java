package com.upc.qhurinet.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muchos usuarios -> un rol. Los roles son excluyentes (US 31-EP4).
    // Sin cascade: eliminar un usuario NO debe eliminar el rol.
    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(length = 150, nullable = false)
    private String nombreCompleto;

    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String fotoPerfilUrl;

    @Column(columnDefinition = "text")
    private String descripcion;

    // tarjeta, yape, plin, transferencia, efectivo; se valida en el servicio
    @Column(length = 20)
    private String metodoPagoPreferido;

    @Column(nullable = false)
    private boolean enLinea = true;

    @Column(precision = 3, scale = 2)
    private BigDecimal calificacionPromedio = BigDecimal.ZERO;

    // pendiente_verificacion, activo; pasa a activo al confirmar el correo (US 25-EP4)
    @Column(length = 30, nullable = false)
    private String estado = "pendiente_verificacion";

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // Borrado en cascada del modelo (notificaciones -> usuarios), a nivel de aplicacion
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notificacion> notificaciones = new ArrayList<>();
    @ManyToMany
    @JoinTable(name = "usuarios_materiales", joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_material_id"))
    private java.util.Set<CategoriaMaterial> materiales = new java.util.HashSet<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MetodoPagoUsuario> metodosPago = new ArrayList<>();

}
