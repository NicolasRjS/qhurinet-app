package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.Rol;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.UsuarioRepositorio;
import com.upc.qhurinet.security.util.JwtUtil;
import com.upc.qhurinet.services.AlmacenamientoService;
import com.upc.qhurinet.services.RolService;
import com.upc.qhurinet.services.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/*
 Guia de errores del proyecto (los traduce GlobalExceptionHandler):
   IllegalArgumentException      -> 400  dato invalido. Mensaje "campo: detalle"; varios se unen con ", "
   AccessDeniedException         -> 403  el usuario no es dueño o parte del recurso
   NoSuchElementException        -> 404  recurso no encontrado
   IllegalStateException         -> 409  conflicto de estado o de unicidad
   UnsupportedOperationException -> 501  metodo PENDIENTE (falta la consulta o un servicio externo)
*/
@Service
@Slf4j
public class UsuarioServiceImpl implements UsuarioService {
    // Valores admitidos en usuarios.estado
    public static final String ESTADO_PENDIENTE = "pendiente_verificacion";
    public static final String ESTADO_ACTIVO = "activo";
    // Valores admitidos en usuarios.metodo_pago_preferido
    public static final List<String> METODOS_PAGO = List.of("tarjeta", "yape", "plin", "transferencia", "efectivo");

    private static final int PASSWORD_MIN = 8;          // US 25-EP4
    private static final int NOMBRE_MAX = 150;          // usuarios.nombre_completo varchar(150)
    private static final int EMAIL_MAX = 150;           // usuarios.email varchar(150)
    private static final int DESCRIPCION_MAX = 500;     // US 32-EP4
    private static final String PATRON_TELEFONO = "\\d{9}";               // formato local de 9 digitos
    private static final String PATRON_EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final String PATRON_ETIQUETA = ".*<[^>]*>.*";          // US 32-EP4: sin etiquetas

    private static final String CARPETA_FOTOS_PERFIL = "fotos_perfil";
    private static final List<String> FORMATOS_IMAGEN = List.of("jpg", "jpeg", "png", "webp");

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private RolService rolService;
    @Autowired
    private AlmacenamientoService almacenamientoService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    @Override
    public UsuarioDTO registrar(RegistrarUsuarioDTO registrarUsuarioDTO) {
        String email = normalizarEmail(registrarUsuarioDTO.getEmail());

        List<String> errores = new ArrayList<>();
        validarNombre(registrarUsuarioDTO.getNombreCompleto(), errores);
        if (email.isEmpty()) {
            errores.add("email: es obligatorio");
        } else if (!email.matches(PATRON_EMAIL) || email.length() > EMAIL_MAX) {
            errores.add("email: no tiene un formato válido");
        }
        if (registrarUsuarioDTO.getPassword() == null || registrarUsuarioDTO.getPassword().length() < PASSWORD_MIN) {
            errores.add("password: debe tener al menos " + PASSWORD_MIN + " caracteres");
        }
        validarTelefono(registrarUsuarioDTO.getTelefono(), errores);
        if (registrarUsuarioDTO.getRolId() == null) {
            errores.add("rolId: es obligatorio");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }

        Rol rol = rolService.buscarRolDeRegistro(registrarUsuarioDTO.getRolId());
        if (usuarioRepositorio.existsByEmail(email)) {
            throw new IllegalStateException("El correo electrónico ya está en uso");
        }

        // Se arma la entidad a mano: el password se cifra y el estado lo asigna el servidor
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(registrarUsuarioDTO.getNombreCompleto().trim());
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordEncoder.encode(registrarUsuarioDTO.getPassword()));
        usuario.setTelefono(registrarUsuarioDTO.getTelefono());
        usuario.setRol(rol);
        usuario = usuarioRepositorio.save(usuario);

        // No hay servicio de correo: el token de confirmacion se deja en el log para probar END-04
        String tokenVerificacion = jwtUtil.generarTokenVerificacion(email);
        log.info("Token de verificación de correo para {}: {}", email, tokenVerificacion);

        return modelMapper.map(usuario, UsuarioDTO.class);
    }

    @Override
    public UsuarioDTO buscarPorEmail(String email) {
        return modelMapper.map(buscarUsuario(email), UsuarioDTO.class);
    }

    @Transactional
    @Override
    public UsuarioDTO verificarCorreo(String email) {
        Usuario usuario = buscarUsuario(email);
        if (!ESTADO_PENDIENTE.equals(usuario.getEstado())) {
            throw new IllegalStateException("La cuenta ya fue verificada");
        }
        usuario.setEstado(ESTADO_ACTIVO);
        return modelMapper.map(usuarioRepositorio.save(usuario), UsuarioDTO.class);
    }

    @Override
    public PerfilUsuarioDTO obtenerPerfil(String email) {
        return modelMapper.map(buscarUsuario(email), PerfilUsuarioDTO.class);
    }

    @Transactional
    @Override
    public PerfilUsuarioDTO actualizarPerfil(String email, ActualizarPerfilDTO actualizarPerfilDTO) {
        List<String> errores = new ArrayList<>();
        validarNombre(actualizarPerfilDTO.getNombreCompleto(), errores);
        validarTelefono(actualizarPerfilDTO.getTelefono(), errores);
        String descripcion = actualizarPerfilDTO.getDescripcion();
        if (descripcion != null && descripcion.length() > DESCRIPCION_MAX) {
            errores.add("descripcion: no puede superar los " + DESCRIPCION_MAX + " caracteres");
        } else if (descripcion != null && descripcion.matches("(?s)" + PATRON_ETIQUETA)) {
            errores.add("descripcion: no admite etiquetas ni contenido con formato");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }

        // Solo se modifican los campos editables; el email identifica la cuenta y no cambia
        Usuario usuario = buscarUsuario(email);
        usuario.setNombreCompleto(actualizarPerfilDTO.getNombreCompleto().trim());
        usuario.setTelefono(actualizarPerfilDTO.getTelefono());
        usuario.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion);
        return modelMapper.map(usuarioRepositorio.save(usuario), PerfilUsuarioDTO.class);
    }

    @Transactional
    @Override
    public FotoPerfilDTO actualizarFotoPerfil(String email, MultipartFile archivo) {
        Usuario usuario = buscarUsuario(email);
        String ruta = almacenamientoService.guardar(archivo, CARPETA_FOTOS_PERFIL, FORMATOS_IMAGEN);
        usuario.setFotoPerfilUrl(ruta);
        usuarioRepositorio.save(usuario);
        return new FotoPerfilDTO(ruta);
    }

    @Transactional
    @Override
    public DisponibilidadDTO actualizarDisponibilidad(String email, DisponibilidadDTO disponibilidadDTO) {
        if (disponibilidadDTO.getEnLinea() == null) {
            throw new IllegalArgumentException("enLinea: es obligatorio (true o false)");
        }
        Usuario usuario = buscarUsuario(email);
        usuario.setEnLinea(disponibilidadDTO.getEnLinea());
        usuarioRepositorio.save(usuario);
        return new DisponibilidadDTO(usuario.isEnLinea());
    }

    @Transactional
    @Override
    public MetodoPagoDTO actualizarMetodoPago(String email, MetodoPagoDTO metodoPagoDTO) {
        String metodoPago = metodoPagoDTO.getMetodoPagoPreferido();
        if (metodoPago == null || !METODOS_PAGO.contains(metodoPago)) {
            throw new IllegalArgumentException("metodoPagoPreferido: debe ser uno de " + String.join(", ", METODOS_PAGO));
        }
        Usuario usuario = buscarUsuario(email);
        usuario.setMetodoPagoPreferido(metodoPago);
        usuarioRepositorio.save(usuario);
        return new MetodoPagoDTO(usuario.getMetodoPagoPreferido());
    }

    @Override
    public ReputacionUsuarioDTO obtenerReputacion(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        // PENDIENTE (query): total de solicitudes 'ejecutada' donde el usuario es recolector y si tiene
        // algun documento 'aprobado'. Con eso: new ReputacionUsuarioDTO(usuario.getId(), usuario.getNombreCompleto(),
        // usuario.getFotoPerfilUrl(), calificacionPromedio o null si no tiene entregas calificadas (US 10-EP2), total, verificado)
        throw new UnsupportedOperationException("END-12 pendiente: falta la consulta de entregas y documentos del usuario " + usuario.getId());
    }

    @Override
    public Usuario obtenerUsuario(String email) {
        return buscarUsuario(email);
    }

    private Usuario buscarUsuario(String email) {
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private void validarNombre(String nombreCompleto, List<String> errores) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            errores.add("nombreCompleto: es obligatorio");
        } else if (nombreCompleto.trim().length() > NOMBRE_MAX) {
            errores.add("nombreCompleto: no puede superar los " + NOMBRE_MAX + " caracteres");
        }
    }

    private void validarTelefono(String telefono, List<String> errores) {
        if (telefono == null || telefono.isBlank()) {
            errores.add("telefono: es obligatorio");
        } else if (!telefono.matches(PATRON_TELEFONO)) {
            errores.add("telefono: debe tener 9 dígitos");
        }
    }
}
