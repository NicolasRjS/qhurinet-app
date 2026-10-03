package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.Rol;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.UsuarioRepositorio;
import com.upc.qhurinet.repositories.DocumentoVerificacionRepositorio;
import com.upc.qhurinet.repositories.SolicitudRecoleccionRepositorio;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/*
 Guia de errores del proyecto (los traduce GlobalExceptionHandler):
   IllegalArgumentException      -> 400  dato invalido. Mensaje "campo: detalle"; varios se unen con ", "
   AccessDeniedException         -> 403  el usuario no es dueño o parte del recurso
   NoSuchElementException        -> 404  recurso no encontrado
   IllegalStateException         -> 409  conflicto de estado o de unicidad
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
    private SolicitudRecoleccionRepositorio solicitudRecoleccionRepositorio;
    @Autowired
    private DocumentoVerificacionRepositorio documentoVerificacionRepositorio;
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
    @Autowired
    private com.upc.qhurinet.repositories.CategoriaMaterialRepositorio categoriaMaterialRepositorio;
    @Autowired
    private com.upc.qhurinet.services.CorreoService correoService;

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

        // SMTP en entrega; modo log configurable para desarrollo local.
        String tokenVerificacion = jwtUtil.generarTokenVerificacion(email);
        correoService.enviarVerificacion(email, tokenVerificacion);

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
        return perfil(buscarUsuario(email));
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
        Usuario usuario = usuarioRepositorio.buscarParaActualizar(buscarUsuario(email).getId()).orElseThrow();
        if (actualizarPerfilDTO.getMaterialesIds() != null) {
            java.util.Set<com.upc.qhurinet.entities.CategoriaMaterial> materiales = new java.util.HashSet<>();
            for (Integer id : actualizarPerfilDTO.getMaterialesIds()) {
                if (id == null) throw new IllegalArgumentException("materialesIds: no admite null");
                materiales.add(categoriaMaterialRepositorio.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Categoría de material no encontrada")));
            }
            usuario.setMateriales(materiales);
        }
        usuario.setNombreCompleto(actualizarPerfilDTO.getNombreCompleto().trim());
        usuario.setTelefono(actualizarPerfilDTO.getTelefono());
        usuario.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion);
        return perfil(usuarioRepositorio.save(usuario));
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
        Usuario usuario = usuarioRepositorio.buscarParaActualizar(buscarUsuario(email).getId()).orElseThrow();
        List<MetodoPagoUsuarioDTO> datos = metodoPagoDTO.getMetodos();
        if (datos == null) {
            String tipo = metodoPagoDTO.getMetodoPagoPreferido();
            if (tipo == null || !METODOS_PAGO.contains(tipo)) throw new IllegalArgumentException("metodoPagoPreferido: no admitido");
            var existente = usuario.getMetodosPago().stream().filter(m -> m.getTipo().equals(tipo)).findFirst();
            if (existente.isPresent()) {
                usuario.getMetodosPago().forEach(m -> m.setPredeterminado(m == existente.get()));
            } else {
                if (!"efectivo".equals(tipo)) throw new IllegalArgumentException("metodos: envíe los datos requeridos del método nuevo");
                usuario.getMetodosPago().forEach(m -> m.setPredeterminado(false));
                usuario.getMetodosPago().add(new com.upc.qhurinet.entities.MetodoPagoUsuario(null, usuario, tipo, null, true));
            }
        } else {
            if (datos.isEmpty() || datos.size() > 10) throw new IllegalArgumentException("metodos: entre 1 y 10 métodos");
            if (datos.stream().anyMatch(java.util.Objects::isNull)) throw new IllegalArgumentException("metodos: no admite null");
            if (datos.stream().filter(m -> Boolean.TRUE.equals(m.getPredeterminado())).count() != 1) {
                throw new IllegalArgumentException("metodos: exactamente uno debe ser predeterminado");
            }
            java.util.Set<Long> ids = new java.util.HashSet<>();
            java.util.List<com.upc.qhurinet.entities.MetodoPagoUsuario> resultado = new java.util.ArrayList<>();
            for (MetodoPagoUsuarioDTO dato : datos) {
                validarMetodo(dato);
                com.upc.qhurinet.entities.MetodoPagoUsuario metodo;
                if (dato.getId() == null) {
                    metodo = new com.upc.qhurinet.entities.MetodoPagoUsuario(); metodo.setUsuario(usuario);
                } else {
                    if (!ids.add(dato.getId())) throw new IllegalArgumentException("metodos: identificador repetido");
                    metodo = usuario.getMetodosPago().stream().filter(m -> dato.getId().equals(m.getId())).findFirst()
                            .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Método ajeno al usuario"));
                }
                metodo.setTipo(dato.getTipo()); metodo.setDato(dato.getDato());
                metodo.setPredeterminado(Boolean.TRUE.equals(dato.getPredeterminado()));
                resultado.add(metodo);
            }
            usuario.getMetodosPago().clear(); usuario.getMetodosPago().addAll(resultado);
        }
        usuario.setMetodoPagoPreferido(usuario.getMetodosPago().stream().filter(m -> m.isPredeterminado()).findFirst().orElseThrow().getTipo());
        usuarioRepositorio.saveAndFlush(usuario);
        return new MetodoPagoDTO(usuario.getMetodoPagoPreferido(), metodos(usuario));
    }

    private void validarMetodo(MetodoPagoUsuarioDTO metodo) {
        if (metodo.getTipo() == null || !METODOS_PAGO.contains(metodo.getTipo())) throw new IllegalArgumentException("tipo: método no admitido");
        String dato = metodo.getDato();
        boolean valido = switch (metodo.getTipo()) {
            case "efectivo" -> dato == null || dato.isBlank();
            case "yape", "plin" -> dato != null && dato.matches("\\d{9}");
            case "tarjeta" -> dato != null && dato.matches("\\d{4}");
            case "transferencia" -> dato != null && dato.matches("\\d{20}");
            default -> false;
        };
        if (!valido) throw new IllegalArgumentException("dato: billetera requiere 9 dígitos; tarjeta solo últimos 4; transferencia CCI de 20; efectivo sin dato");
    }
    private List<MetodoPagoUsuarioDTO> metodos(Usuario u) {
        return u.getMetodosPago().stream().map(m -> new MetodoPagoUsuarioDTO(m.getId(), m.getTipo(), m.getDato(), m.isPredeterminado())).toList();
    }
    private PerfilUsuarioDTO perfil(Usuario u) {
        PerfilUsuarioDTO dto = modelMapper.map(u, PerfilUsuarioDTO.class);
        dto.setMetodosPago(metodos(u));
        dto.setMateriales(u.getMateriales().stream().map(c -> modelMapper.map(c, CategoriaMaterialDTO.class)).toList());
        dto.setVerificado(!documentoVerificacionRepositorio.findByUsuario_IdAndEstado(u.getId(), DocumentoVerificacionServiceImpl.ESTADO_APROBADO).isEmpty());
        if (dto.getCalificacionPromedio() != null && dto.getCalificacionPromedio().signum() == 0) dto.setCalificacionPromedio(null);
        return dto;
    }

    @Override
    public ReputacionUsuarioDTO obtenerReputacion(Long id) {
        Usuario usuario = usuarioRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        boolean generador = RolServiceImpl.GENERADOR.equals(usuario.getRol().getNombre());
        List<SolicitudRecoleccion> entregas = generador
                ? solicitudRecoleccionRepositorio.findByPublicacion_Generador_IdAndEstado(usuario.getId(), SolicitudRecoleccionServiceImpl.EJECUTADA)
                : solicitudRecoleccionRepositorio.findByRecolector_IdAndEstado(usuario.getId(), SolicitudRecoleccionServiceImpl.EJECUTADA);
        List<SolicitudRecoleccion> calificadas = entregas.stream()
                .filter(solicitud -> !generador && solicitud.getCalificacionRecolector() != null)
                .toList();
        BigDecimal promedio = calificadas.isEmpty() ? null : calificadas.stream()
                .map(solicitud -> BigDecimal.valueOf(solicitud.getCalificacionRecolector()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(calificadas.size()), 2, RoundingMode.HALF_UP);
        boolean verificado = !documentoVerificacionRepositorio.findByUsuario_IdAndEstado(
                usuario.getId(), DocumentoVerificacionServiceImpl.ESTADO_APROBADO).isEmpty();
        ReputacionUsuarioDTO dto = new ReputacionUsuarioDTO(usuario.getId(), usuario.getNombreCompleto(),
                usuario.getFotoPerfilUrl(), promedio, (long) entregas.size(), verificado);
        dto.setDescripcion(usuario.getDescripcion()); dto.setRolNombre(usuario.getRol().getNombre());
        dto.setMateriales(usuario.getMateriales().stream().map(c -> modelMapper.map(c, CategoriaMaterialDTO.class)).toList());
        return dto;
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
