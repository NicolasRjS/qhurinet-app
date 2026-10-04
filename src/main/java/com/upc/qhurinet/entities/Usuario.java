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
@Table(
        name = "usuarios",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_usuario_identidad_externa",
                        columnNames = {"proveedor_externo", "id_externo"}))
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

    @Column(length = 255, nullable = false)
    private String passwordHash;

    @Column(length = 20)
    private String proveedorExterno;

    @Column(length = 255)
    private String idExterno;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String fotoPerfilUrl;

    @Column(columnDefinition = "text")
    private String descripcion;

    // tarjeta, yape, plin, transferencia, efectivo; se valida en el servicio
    @Column(length = 20)
    private String metodoPagoPreferido;

    @Column(length = 4)
    private String pagoTarjetaUltimos4;

    @Column(length = 9)
    private String pagoYapeCelular;

    @Column(length = 9)
    private String pagoPlinCelular;

    @Column(length = 20)
    private String pagoTransferenciaCuenta;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean pagoEfectivo;

    // US 29: conservar los ids opacos del contrato al migrar los metodos existentes.
    private Long pagoTarjetaId;

    private Long pagoYapeId;

    private Long pagoPlinId;

    private Long pagoTransferenciaId;

    private Long pagoEfectivoId;

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

    // US 26: ids de categorias_material separados por coma; se validan en el servicio
    @Column(length = 255)
    private String materiales;
}
