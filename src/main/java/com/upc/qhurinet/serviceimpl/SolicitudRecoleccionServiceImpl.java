package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.PublicacionMaterial;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.PublicacionMaterialRepositorio;
import com.upc.qhurinet.repositories.SolicitudRecoleccionRepositorio;
import com.upc.qhurinet.repositories.UsuarioRepositorio;
import com.upc.qhurinet.services.NotificacionService;
import com.upc.qhurinet.services.PublicacionMaterialService;
import com.upc.qhurinet.services.SolicitudRecoleccionService;
import com.upc.qhurinet.services.UsuarioService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SolicitudRecoleccionServiceImpl implements SolicitudRecoleccionService {
    // Valores admitidos en solicitudes_recoleccion.estado
    public static final String CREADA = "creada";
    public static final String COORDINADA = "coordinada";
    public static final String EN_CAMINO = "en_camino";
    public static final String EJECUTADA = "ejecutada";
    public static final String CANCELADA = "cancelada";
    // Valores admitidos en solicitudes_recoleccion.metodo_pago (los mismos de usuarios.metodo_pago_preferido)
    public static final List<String> METODOS_PAGO = UsuarioServiceImpl.METODOS_PAGO;

    private static final List<String> REPROGRAMABLES = List.of(CREADA, COORDINADA, EN_CAMINO);   // US 08-EP2
    private static final List<String> CON_QR_ACTIVO = List.of(COORDINADA, EN_CAMINO);           // US 07-EP2
    private static final List<String> CERRADAS = List.of(EJECUTADA, CANCELADA);

    @Autowired
    private SolicitudRecoleccionRepositorio solicitudRecoleccionRepositorio;
    @Autowired
    private PublicacionMaterialRepositorio publicacionMaterialRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PublicacionMaterialService publicacionMaterialService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private NotificacionService notificacionService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private com.upc.qhurinet.services.DisponibilidadService disponibilidadService;
    @Autowired
    private com.upc.qhurinet.services.SeguimientoService seguimientoService;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    // Reclamar un anuncio: crea la solicitud y reserva la publicacion en una sola transaccion
    @Transactional
    @Override
    public SolicitudDTO crear(String email, CrearSolicitudDTO crearSolicitudDTO) {
        if (crearSolicitudDTO.getPublicacionId() == null) {
            throw new IllegalArgumentException("publicacionId: es obligatorio");
        }
        Usuario recolector = usuarioService.obtenerUsuario(email);
        PublicacionMaterial publicacion = publicacionMaterialRepositorio.buscarParaActualizar(crearSolicitudDTO.getPublicacionId())
                .orElseThrow(() -> new NoSuchElementException("Publicación no encontrada"));
        if (!PublicacionMaterialServiceImpl.DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException("La publicación ya no está disponible");
        }

        SolicitudRecoleccion solicitud = new SolicitudRecoleccion();
        solicitud.setPublicacion(publicacion);
        solicitud.setRecolector(recolector);
        solicitud.setMontoPago(publicacion.getMontoPago());
        solicitud.setMetodoPago(publicacion.getMetodoPago());
        solicitud.setFranjaHoraria(publicacion.getFranjaHoraria());
        solicitud.setCodigoQr(UUID.randomUUID().toString());
        solicitud = solicitudRecoleccionRepositorio.save(solicitud);

        publicacion.setEstado(PublicacionMaterialServiceImpl.RESERVADO);
        publicacionMaterialRepositorio.save(publicacion);
        notificacionService.notificar(publicacion.getGenerador(),
                "Tu publicación de " + nombreMaterial(solicitud) + " fue reclamada por un recolector");
        notificacionService.notificar(recolector, "Reclamaste la publicación de " + nombreMaterial(solicitud));
        return modelMapper.map(solicitud, SolicitudDTO.class);
    }

    @Override
    public List<MiSolicitudDTO> listarMisSolicitudes(String email, String estado) {
        Usuario usuario = usuarioService.obtenerUsuario(email);
        if (estado != null && !List.of("activos", "completados", "cancelados").contains(estado)) {
            throw new IllegalArgumentException("estado: debe ser activos, completados o cancelados");
        }
        return solicitudRecoleccionRepositorio.findByRecolector_IdOrderByPrioritariaDescFechaCoordinadaAsc(usuario.getId())
                .stream()
                .filter(solicitud -> estado == null
                        || ("activos".equals(estado) && REPROGRAMABLES.contains(solicitud.getEstado()))
                        || ("completados".equals(estado) && EJECUTADA.equals(solicitud.getEstado()))
                        || ("cancelados".equals(estado) && CANCELADA.equals(solicitud.getEstado())))
                .map(solicitud -> new MiSolicitudDTO(solicitud.getId(), solicitud.getPublicacion().getCantidad(),
                        nombreMaterial(solicitud), solicitud.getPublicacion().getDireccion(), solicitud.getEstado(),
                        solicitud.isPrioritaria(), solicitud.getPublicacion().getGenerador().getNombreCompleto(),
                        solicitud.getFechaCoordinada(), solicitud.getPublicacion().getId(), solicitud.getFranjaHoraria(),
                        solicitud.getPublicacion().getUnidadMedida()))
                .toList();
    }

    @Transactional
    @Override
    public SolicitudDTO reprogramar(String email, Long id, ReprogramarSolicitudDTO reprogramarSolicitudDTO) {
        java.util.List<String> errores = new java.util.ArrayList<>();
        disponibilidadService.validarFranja(reprogramarSolicitudDTO.getFranjaHoraria(), errores);
        if (!errores.isEmpty()) throw new IllegalArgumentException(String.join(", ", errores));
        LocalDateTime fecha = reprogramarSolicitudDTO.getFechaCoordinada();
        if (fecha == null) {
            throw new IllegalArgumentException("fechaCoordinada: es obligatoria");
        }
        if (fecha.toLocalDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("fechaCoordinada: no puede ser anterior al día actual");
        }
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (!REPROGRAMABLES.contains(solicitud.getEstado())) {
            throw new IllegalStateException("La recolección no admite reprogramación en estado " + solicitud.getEstado());
        }
        // El codigo QR no se regenera: la recoleccion es la misma (US 08-EP2)
        solicitud.setFechaCoordinada(fecha);
        solicitud.setFranjaHoraria(reprogramarSolicitudDTO.getFranjaHoraria());
        seguimientoService.eliminar(id);
        solicitud.setEstado(COORDINADA);
        solicitud = solicitudRecoleccionRepositorio.save(solicitud);
        notificarContraparte(solicitud, email, "La recolección de " + nombreMaterial(solicitud) + " fue reprogramada para el " + fecha);
        notificarAutor(solicitud, email, "Reprogramaste la recolección de " + nombreMaterial(solicitud) + " para el " + fecha);
        return modelMapper.map(solicitud, SolicitudDTO.class);
    }

    // Cancela la solicitud y devuelve la publicacion al mapa en una sola transaccion (US 09-EP2)
    @Transactional
    @Override
    public SolicitudDTO cancelar(String email, Long id, CancelarSolicitudDTO cancelarSolicitudDTO) {
        String motivo = cancelarSolicitudDTO.getMotivo();
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("motivo: es obligatorio");
        }
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (CERRADAS.contains(solicitud.getEstado())) {
            throw new IllegalStateException("Una recolección " + solicitud.getEstado() + " no puede cancelarse");
        }
        if (motivo.length() > 500) throw new IllegalArgumentException("motivo: máximo 500 caracteres");
        seguimientoService.eliminar(id);
        solicitud.setEstado(CANCELADA);
        solicitud.setObservaciones(motivo.trim());
        solicitud = solicitudRecoleccionRepositorio.save(solicitud);

        PublicacionMaterial publicacion = solicitud.getPublicacion();
        publicacion.setEstado(PublicacionMaterialServiceImpl.DISPONIBLE);
        publicacionMaterialRepositorio.save(publicacion);
        notificarContraparte(solicitud, email, "La recolección de " + nombreMaterial(solicitud) + " fue cancelada. Motivo: " + motivo.trim());
        notificarAutor(solicitud, email, "Cancelaste la recolección de " + nombreMaterial(solicitud) + ". Motivo: " + motivo.trim());
        return modelMapper.map(solicitud, SolicitudDTO.class);
    }

    @Transactional
    @Override
    public SolicitudDTO cambiarPrioridad(String email, Long id, PrioridadSolicitudDTO prioridadSolicitudDTO) {
        if (prioridadSolicitudDTO.getPrioritaria() == null) {
            throw new IllegalArgumentException("prioritaria: es obligatorio (true o false)");
        }
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (CERRADAS.contains(solicitud.getEstado())) {
            throw new IllegalStateException("Una recolección " + solicitud.getEstado() + " no admite cambios de prioridad");
        }
        solicitud.setPrioritaria(prioridadSolicitudDTO.getPrioritaria());
        return modelMapper.map(solicitudRecoleccionRepositorio.save(solicitud), SolicitudDTO.class);
    }

    // El generador muestra el codigo; el recolector asignado lo escanea (US 07-EP2)
    @Override
    public CodigoQrDTO obtenerCodigoQr(String email, Long id) {
        SolicitudRecoleccion solicitud = obtenerSolicitud(id);
        if (!esGenerador(solicitud, email)) {
            throw new AccessDeniedException("Solo el generador de la publicación puede ver el código");
        }
        if (!CON_QR_ACTIVO.contains(solicitud.getEstado())) {
            throw new IllegalStateException("El código solo está disponible para recolecciones coordinadas o en camino");
        }
        return new CodigoQrDTO(solicitud.getCodigoQr(), solicitud.isQrValidado());
    }

    @Override
    public EntregaQrDTO validarQr(String email, Long id, ValidarQrDTO validarQrDTO) {
        if (validarQrDTO.getCodigo() == null || validarQrDTO.getCodigo().isBlank()) {
            throw new IllegalArgumentException("codigo: es obligatorio");
        }
        SolicitudRecoleccion solicitud = obtenerSolicitudComoRecolector(email, id);
        validarEstadoParaConfirmar(solicitud);
        if (solicitud.isQrValidado() || !solicitud.getCodigoQr().equals(validarQrDTO.getCodigo().trim())) {
            throw new IllegalStateException("Código no válido para esta recolección");
        }
        PublicacionMaterial publicacion = solicitud.getPublicacion();
        return new EntregaQrDTO(solicitud.getId(), publicacion.getGenerador().getNombreCompleto(),
                publicacion.getDireccion(), nombreMaterial(solicitud), publicacion.getCantidad());
    }

    // Confirma la entrega: solicitud ejecutada y publicacion recolectada en una sola transaccion (US 07-EP2)
    @Transactional
    @Override
    public SolicitudDTO confirmarEntrega(String email, Long id, ValidarQrDTO datos) {
        if (datos == null || datos.getCodigo() == null || datos.getCodigo().isBlank()) {
            throw new IllegalArgumentException("codigo: es obligatorio");
        }
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (!esRecolector(solicitud, email)) throw new AccessDeniedException("Solo el recolector asignado puede confirmar");
        if (!datos.getCodigo().trim().equals(solicitud.getCodigoQr())) throw new IllegalStateException("Código no válido para esta recolección");
        // Idempotencia: una solicitud ya confirmada no cambia ni genera otra notificacion
        if (solicitud.isQrValidado()) {
            throw new IllegalStateException("La entrega ya fue confirmada");
        }
        validarEstadoParaConfirmar(solicitud);
        LocalDateTime ahora = LocalDateTime.now();
        seguimientoService.eliminar(id);
        solicitud.setEstado(EJECUTADA);
        solicitud.setQrValidado(true);
        solicitud.setFechaValidacion(ahora);
        solicitud.setFechaEjecucion(ahora);
        solicitud = solicitudRecoleccionRepositorio.save(solicitud);

        PublicacionMaterial publicacion = solicitud.getPublicacion();
        publicacion.setEstado(PublicacionMaterialServiceImpl.RECOLECTADO);
        publicacionMaterialRepositorio.save(publicacion);
        String mensaje = "Entrega confirmada: " + publicacion.getCantidad() + " " + publicacion.getUnidadMedida()
                + " de " + nombreMaterial(solicitud);
        notificacionService.notificar(publicacion.getGenerador(), mensaje);
        notificacionService.notificar(solicitud.getRecolector(), mensaje);
        return modelMapper.map(solicitud, SolicitudDTO.class);
    }

    @Transactional
    @Override
    public CalificacionDTO calificar(String email, Long id, CalificarRecolectorDTO calificarRecolectorDTO) {
        Integer calificacion = calificarRecolectorDTO.getCalificacion();
        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("calificacion: debe ser un entero entre 1 y 5");
        }
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (!esGenerador(solicitud, email)) {
            throw new AccessDeniedException("Solo el generador de la publicación puede calificar la recolección");
        }
        if (!EJECUTADA.equals(solicitud.getEstado())) {
            throw new IllegalStateException("Solo puede calificarse una recolección ejecutada");
        }
        if (solicitud.getCalificacionRecolector() != null) {
            throw new IllegalStateException("La recolección ya fue calificada");
        }
        String comentario = calificarRecolectorDTO.getComentario();
        if (comentario != null && (comentario.length() > 200 || comentario.matches("(?s).*<[^>]*>.*"))) {
            throw new IllegalArgumentException("comentario: máximo 200 caracteres, sin HTML");
        }
        solicitud.setComentarioCalificacion(comentario);
        solicitud.setCalificacionRecolector(calificacion);
        solicitudRecoleccionRepositorio.save(solicitud);

        Usuario recolector = usuarioRepositorio.buscarParaActualizar(solicitud.getRecolector().getId()).orElseThrow();
        List<SolicitudRecoleccion> calificadas = solicitudRecoleccionRepositorio.buscarCalificadasPorRecolector(
                recolector.getId(), EJECUTADA);
        BigDecimal promedio = calificadas.stream()
                .map(calificada -> BigDecimal.valueOf(calificada.getCalificacionRecolector()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(calificadas.size()), 2, RoundingMode.HALF_UP);
        recolector.setCalificacionPromedio(promedio);
        usuarioRepositorio.save(recolector);
        return new CalificacionDTO(calificacion, promedio);
    }

    @Override
    public SeguimientoDTO obtenerSeguimiento(String email, Long id) {
        SolicitudRecoleccion solicitud = obtenerSolicitud(id);
        if (!esGenerador(solicitud, email)) {
            throw new AccessDeniedException("Solo el generador de la publicación puede ver el seguimiento");
        }
        if (!EN_CAMINO.equals(solicitud.getEstado())) {
            throw new IllegalStateException("El seguimiento solo está disponible cuando la recolección está en camino");
        }
        return seguimientoService.obtener(solicitud);

    }

    @Override
    public SolicitudRecoleccion obtenerSolicitudComoParte(String email, Long id) {
        SolicitudRecoleccion solicitud = obtenerSolicitud(id);
        if (!esGenerador(solicitud, email) && !esRecolector(solicitud, email)) {
            throw new AccessDeniedException("El usuario no es parte de esta recolección");
        }
        return solicitud;
    }

    private SolicitudRecoleccion obtenerSolicitud(Long id) {
        return solicitudRecoleccionRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recolección no encontrada"));
    }

    // Solo el recolector asignado puede validar y confirmar (US 07-EP2 criterio 5)
    private SolicitudRecoleccion obtenerSolicitudComoRecolector(String email, Long id) {
        SolicitudRecoleccion solicitud = obtenerSolicitud(id);
        if (!esRecolector(solicitud, email)) {
            throw new AccessDeniedException("Solo el recolector asignado puede confirmar la entrega");
        }
        return solicitud;
    }

    private void validarEstadoParaConfirmar(SolicitudRecoleccion solicitud) {
        if (!CON_QR_ACTIVO.contains(solicitud.getEstado())) {
            throw new IllegalStateException("La recolección no se encuentra en un estado válido para confirmarse");
        }
    }

    private boolean esGenerador(SolicitudRecoleccion solicitud, String email) {
        return solicitud.getPublicacion().getGenerador().getEmail().equals(email);
    }

    private boolean esRecolector(SolicitudRecoleccion solicitud, String email) {
        return solicitud.getRecolector() != null && solicitud.getRecolector().getEmail().equals(email);
    }

    private void notificarContraparte(SolicitudRecoleccion solicitud, String email, String mensaje) {
        Usuario contraparte = esGenerador(solicitud, email) ? solicitud.getRecolector() : solicitud.getPublicacion().getGenerador();
        if (contraparte != null) {
            notificacionService.notificar(contraparte, mensaje);
        }
    }

    private void notificarAutor(SolicitudRecoleccion solicitud, String email, String mensaje) {
        Usuario autor = esGenerador(solicitud, email) ? solicitud.getPublicacion().getGenerador() : solicitud.getRecolector();
        if (autor != null) {
            notificacionService.notificar(autor, mensaje);
        }
    }

    private String nombreMaterial(SolicitudRecoleccion solicitud) {
        return solicitud.getPublicacion().getCategoriaMaterial().getNombre();
    }
    private SolicitudRecoleccion obtenerParaActualizar(String email, Long id) {
        SolicitudRecoleccion solicitud = obtenerSolicitudComoParte(email, id);
        publicacionMaterialRepositorio.buscarParaActualizar(solicitud.getPublicacion().getId()).orElseThrow();
        // Refrescar evita usar un estado leido antes de esperar por el bloqueo.
        entityManager.refresh(solicitud, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        entityManager.refresh(solicitud.getPublicacion());
        return solicitud;
    }
    @Override @Transactional
    public SolicitudDTO coordinar(String email, Long id, ReprogramarSolicitudDTO datos) {
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (!CREADA.equals(solicitud.getEstado())) throw new IllegalStateException("Solo se coordina una solicitud creada");
        return reprogramar(email, id, datos);
    }
    @Override @Transactional
    public SolicitudDTO iniciar(String email, Long id) {
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (!esRecolector(solicitud, email)) throw new AccessDeniedException("Solo el recolector asignado puede iniciar");
        if (!COORDINADA.equals(solicitud.getEstado())) throw new IllegalStateException("La recolección debe estar coordinada");
        solicitud.setEstado(EN_CAMINO);
        solicitudRecoleccionRepositorio.save(solicitud);
        notificacionService.notificar(solicitud.getPublicacion().getGenerador(), "El recolector está en camino");
        notificacionService.notificar(solicitud.getRecolector(), "Iniciaste el recorrido de la recolección");
        return modelMapper.map(solicitud, SolicitudDTO.class);
    }
    @Override @Transactional
    public void actualizarUbicacion(String email, Long id, UbicacionDTO datos) {
        SolicitudRecoleccion solicitud = obtenerParaActualizar(email, id);
        if (!esRecolector(solicitud, email)) throw new AccessDeniedException("Solo el recolector asignado puede enviar ubicación");
        if (!EN_CAMINO.equals(solicitud.getEstado())) throw new IllegalStateException("La recolección no está en camino");
        seguimientoService.actualizar(id, datos);
    }
    @Override
    public DetalleSolicitudDTO obtenerDetalle(String email, Long id) {
        SolicitudRecoleccion s = obtenerSolicitudComoParte(email, id);
        PublicacionMaterial p = s.getPublicacion();
        return new DetalleSolicitudDTO(modelMapper.map(s, SolicitudDTO.class), p.getGenerador().getId(),
                s.getRecolector().getId(), esGenerador(s, email) ? s.getRecolector().getNombreCompleto() : p.getGenerador().getNombreCompleto(),
                nombreMaterial(s), p.getCantidad(), p.getUnidadMedida(), p.getDireccion());
    }

}
