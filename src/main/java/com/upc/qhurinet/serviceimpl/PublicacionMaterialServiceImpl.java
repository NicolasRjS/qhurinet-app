package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.CancelarSolicitudDTO;
import com.upc.qhurinet.dtos.CrearPublicacionDTO;
import com.upc.qhurinet.dtos.EditarPublicacionDTO;
import com.upc.qhurinet.dtos.FotoPublicacionDTO;
import com.upc.qhurinet.dtos.MiPublicacionDTO;
import com.upc.qhurinet.dtos.PublicacionDTO;
import com.upc.qhurinet.entities.CategoriaMaterial;
import com.upc.qhurinet.entities.PublicacionMaterial;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.PublicacionMaterialRepositorio;
import com.upc.qhurinet.services.AlmacenamientoService;
import com.upc.qhurinet.services.CategoriaMaterialService;
import com.upc.qhurinet.services.DisponibilidadService;
import com.upc.qhurinet.services.PublicacionMaterialService;
import com.upc.qhurinet.services.UsuarioService;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class PublicacionMaterialServiceImpl implements PublicacionMaterialService {

    // Valores admitidos en publicaciones_material.estado
    public static final String DISPONIBLE = "disponible";

    public static final String RESERVADO = "reservado";

    public static final String RECOLECTADO = "recolectado";

    public static final String CANCELADO = "cancelado";

    // US 02-EP1
    private static final int DESCRIPCION_MAX = 200;

    // US 02-EP1: sin HTML
    private static final String PATRON_ETIQUETA = "(?s).*<[^>]*>.*";

    private static final String CARPETA_FOTOS = "fotos_publicaciones";

    private static final List<String> FORMATOS_IMAGEN = List.of("jpg", "jpeg", "png", "webp");

    @Autowired
    private PublicacionMaterialRepositorio publicacionMaterialRepositorio;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CategoriaMaterialService categoriaMaterialService;

    @Autowired
    private AlmacenamientoService almacenamientoService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Transactional
    @Override
    public PublicacionDTO crear(String email, CrearPublicacionDTO crearPublicacionDTO) {
        List<String> errores = new ArrayList<>();
        if (crearPublicacionDTO.getCategoriaMaterialId() == null) {
            errores.add("categoriaMaterialId: es obligatorio");
        }
        validarCampos(
                crearPublicacionDTO.getCantidad(),
                crearPublicacionDTO.getDireccion(),
                crearPublicacionDTO.getFechaDisponibilidad(),
                crearPublicacionDTO.getDescripcion(),
                errores);
        if (crearPublicacionDTO.getLatitud() == null || crearPublicacionDTO.getLongitud() == null) {
            errores.add("latitud y longitud: son obligatorias");
        }
        disponibilidadService.validarFranja(crearPublicacionDTO.getFranjaHoraria(), errores);
        validarPago(
                crearPublicacionDTO.getMontoPago(), crearPublicacionDTO.getMetodoPago(), errores);
        validarCoordenadas(
                crearPublicacionDTO.getLatitud(), crearPublicacionDTO.getLongitud(), errores);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }
        Usuario generador = usuarioService.obtenerUsuario(email);
        if (!UsuarioServiceImpl.ESTADO_ACTIVO.equals(generador.getEstado())) {
            throw new AccessDeniedException("La cuenta debe estar activa para publicar material");
        }
        CategoriaMaterial categoria =
                categoriaMaterialService.obtenerCategoria(
                        crearPublicacionDTO.getCategoriaMaterialId());
        // El generador sale del token; el estado y la fecha de publicacion los asigna el servidor
        PublicacionMaterial publicacion = new PublicacionMaterial();
        publicacion.setGenerador(generador);
        publicacion.setCategoriaMaterial(categoria);
        publicacion.setCantidad(crearPublicacionDTO.getCantidad());
        publicacion.setUnidadMedida(
                crearPublicacionDTO.getUnidadMedida() == null
                                || crearPublicacionDTO.getUnidadMedida().isBlank()
                        ? categoria.getUnidadMedidaDefault()
                        : crearPublicacionDTO.getUnidadMedida());
        publicacion.setDescripcion(crearPublicacionDTO.getDescripcion());
        if (crearPublicacionDTO.getFotoUrl() != null
                && !crearPublicacionDTO.getFotoUrl().isBlank()) {
            throw new IllegalArgumentException("fotoUrl: use la carga de foto de la publicación");
        }
        publicacion.setDireccion(crearPublicacionDTO.getDireccion().trim());
        publicacion.setDistrito(crearPublicacionDTO.getDistrito());
        publicacion.setLatitud(crearPublicacionDTO.getLatitud());
        publicacion.setLongitud(crearPublicacionDTO.getLongitud());
        publicacion.setFechaDisponibilidad(crearPublicacionDTO.getFechaDisponibilidad());
        publicacion.setFranjaHoraria(crearPublicacionDTO.getFranjaHoraria());
        publicacion.setMontoPago(crearPublicacionDTO.getMontoPago());
        publicacion.setMetodoPago(
                crearPublicacionDTO.getMetodoPago() == null
                                && crearPublicacionDTO.getMontoPago() != null
                        ? generador.getMetodoPagoPreferido()
                        : crearPublicacionDTO.getMetodoPago());
        validarUnidad(publicacion.getUnidadMedida());
        return modelMapper.map(
                publicacionMaterialRepositorio.save(publicacion), PublicacionDTO.class);
    }

    @Override
    public PublicacionDTO buscarPorId(Long id) {
        return modelMapper.map(obtenerPublicacion(id), PublicacionDTO.class);
    }

    @Override
    public List<PublicacionDTO> listarParaMapa(
            Integer material,
            String distrito,
            String q,
            BigDecimal minKg,
            LocalDate fecha,
            String estado) {
        List<String> estados = List.of(DISPONIBLE, RESERVADO, RECOLECTADO, CANCELADO);
        if (estado != null && !estados.contains(estado)) {
            throw new IllegalArgumentException(
                    "estado: debe ser uno de " + String.join(", ", estados));
        }
        if (minKg != null && minKg.signum() < 0) {
            throw new IllegalArgumentException("min_kg: no puede ser negativo");
        }
        String distritoBuscado = distrito == null || distrito.isBlank() ? null : distrito.trim();
        String textoBuscado = q == null || q.isBlank() ? null : q.trim().toLowerCase(Locale.ROOT);
        return publicacionMaterialRepositorio.findByEstado(DISPONIBLE).stream()
                .filter(publicacion -> estado == null || DISPONIBLE.equals(estado))
                .filter(
                        publicacion ->
                                material == null
                                        || publicacion
                                                .getCategoriaMaterial()
                                                .getId()
                                                .equals(material))
                .filter(
                        publicacion ->
                                distritoBuscado == null
                                        || publicacion.getDistrito() != null
                                                && publicacion
                                                        .getDistrito()
                                                        .equalsIgnoreCase(distritoBuscado))
                .filter(
                        publicacion ->
                                textoBuscado == null
                                        || contiene(publicacion.getDireccion(), textoBuscado)
                                        || contiene(publicacion.getDistrito(), textoBuscado))
                .filter(publicacion -> cumpleCantidadMinima(publicacion, minKg))
                .filter(
                        publicacion ->
                                fecha == null || fecha.equals(publicacion.getFechaDisponibilidad()))
                .map(publicacion -> modelMapper.map(publicacion, PublicacionDTO.class))
                .toList();
    }

    @Override
    public List<MiPublicacionDTO> listarMisPublicaciones(String email, String estado) {
        Usuario generador = usuarioService.obtenerUsuario(email);
        if (estado != null && !List.of("activos", "completados", "cancelados").contains(estado)) {
            throw new IllegalArgumentException(
                    "estado: debe ser activos, completados o cancelados");
        }
        List<PublicacionMaterial> publicaciones =
                publicacionMaterialRepositorio.findByGenerador_IdOrderByFechaPublicacionDesc(
                        generador.getId());
        List<SolicitudRecoleccion> solicitudes =
                publicacionMaterialRepositorio.buscarSolicitudesVigentesPorGenerador(
                        generador.getId());
        Map<Long, SolicitudRecoleccion> solicitudesPorPublicacion = new HashMap<>();
        for (SolicitudRecoleccion solicitud : solicitudes) {
            solicitudesPorPublicacion.putIfAbsent(solicitud.getPublicacion().getId(), solicitud);
        }
        return publicaciones.stream()
                .filter(
                        publicacion ->
                                estado == null
                                        || "activos".equals(estado)
                                                && (DISPONIBLE.equals(publicacion.getEstado())
                                                        || RESERVADO.equals(
                                                                publicacion.getEstado()))
                                        || "completados".equals(estado)
                                                && RECOLECTADO.equals(publicacion.getEstado())
                                        || "cancelados".equals(estado)
                                                && CANCELADO.equals(publicacion.getEstado()))
                .map(
                        publicacion -> {
                            SolicitudRecoleccion solicitud =
                                    solicitudesPorPublicacion.get(publicacion.getId());
                            String reciclador =
                                    solicitud == null || solicitud.getRecolector() == null
                                            ? null
                                            : solicitud.getRecolector().getNombreCompleto();
                            MiPublicacionDTO dto =
                                    new MiPublicacionDTO(
                                            publicacion.getId(),
                                            publicacion.getCantidad(),
                                            publicacion.getCategoriaMaterial().getNombre(),
                                            publicacion.getFechaPublicacion(),
                                            publicacion.getEstado(),
                                            reciclador,
                                            solicitud == null
                                                    ? null
                                                    : solicitud.getFechaCoordinada());
                            dto.setSolicitudId(solicitud == null ? null : solicitud.getId());
                            dto.setFranjaHoraria(
                                    solicitud == null
                                            ? publicacion.getFranjaHoraria()
                                            : solicitud.getFranjaHoraria());
                            dto.setUnidadMedida(publicacion.getUnidadMedida());
                            return dto;
                        })
                .toList();
    }

    @Transactional
    @Override
    public PublicacionDTO editar(String email, Long id, EditarPublicacionDTO editarPublicacionDTO) {
        List<String> errores = new ArrayList<>();
        validarCampos(
                editarPublicacionDTO.getCantidad(),
                editarPublicacionDTO.getDireccion(),
                editarPublicacionDTO.getFechaDisponibilidad(),
                editarPublicacionDTO.getDescripcion(),
                errores);
        disponibilidadService.validarFranja(editarPublicacionDTO.getFranjaHoraria(), errores);
        validarPago(
                editarPublicacionDTO.getMontoPago(), editarPublicacionDTO.getMetodoPago(), errores);
        if (editarPublicacionDTO.getLatitud() != null
                || editarPublicacionDTO.getLongitud() != null) {
            validarCoordenadas(
                    editarPublicacionDTO.getLatitud(), editarPublicacionDTO.getLongitud(), errores);
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        if (!DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede editar una publicación disponible; para cambiar el horario use"
                        + " la reprogramación");
        }
        publicacion.setCantidad(editarPublicacionDTO.getCantidad());
        publicacion.setDescripcion(editarPublicacionDTO.getDescripcion());
        publicacion.setDireccion(editarPublicacionDTO.getDireccion().trim());
        publicacion.setFechaDisponibilidad(editarPublicacionDTO.getFechaDisponibilidad());
        publicacion.setFranjaHoraria(editarPublicacionDTO.getFranjaHoraria());
        publicacion.setMontoPago(editarPublicacionDTO.getMontoPago());
        publicacion.setMetodoPago(editarPublicacionDTO.getMetodoPago());
        if (editarPublicacionDTO.getCategoriaMaterialId() != null) {
            publicacion.setCategoriaMaterial(
                    categoriaMaterialService.obtenerCategoria(
                            editarPublicacionDTO.getCategoriaMaterialId()));
        }
        if (editarPublicacionDTO.getUnidadMedida() != null) {
            validarUnidad(editarPublicacionDTO.getUnidadMedida());
            publicacion.setUnidadMedida(editarPublicacionDTO.getUnidadMedida());
        }
        if (editarPublicacionDTO.getDistrito() != null) {
            publicacion.setDistrito(editarPublicacionDTO.getDistrito());
        }
        if (editarPublicacionDTO.getLatitud() != null) {
            publicacion.setLatitud(editarPublicacionDTO.getLatitud());
            publicacion.setLongitud(editarPublicacionDTO.getLongitud());
        }
        return modelMapper.map(
                publicacionMaterialRepositorio.save(publicacion), PublicacionDTO.class);
    }

    // US 09-EP2 criterio 8: el generador cancela su publicacion mientras no haya sido reclamada
    @Transactional
    @Override
    public PublicacionDTO cancelar(String email, Long id, CancelarSolicitudDTO datos) {
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        if (!DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede cancelar una publicación que no ha sido reclamada");
        }
        if (datos != null && datos.getMotivo() != null && datos.getMotivo().length() > 500) {
            throw new IllegalArgumentException("motivo: máximo 500 caracteres");
        }
        publicacion.setMotivoCancelacion(datos == null ? null : datos.getMotivo());
        publicacion.setEstado(CANCELADO);
        return modelMapper.map(
                publicacionMaterialRepositorio.save(publicacion), PublicacionDTO.class);
    }

    @Transactional
    @Override
    public FotoPublicacionDTO subirFoto(String email, Long id, MultipartFile archivo) {
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        if (!DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException("La publicación no está disponible");
        }
        String ruta = almacenamientoService.guardar(archivo, CARPETA_FOTOS, FORMATOS_IMAGEN);
        publicacion.setFotoUrl(ruta);
        publicacionMaterialRepositorio.save(publicacion);
        return new FotoPublicacionDTO(ruta);
    }

    @Override
    public PublicacionMaterial obtenerPublicacion(Long id) {
        return publicacionMaterialRepositorio
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Publicación no encontrada"));
    }

    @Override
    @Transactional
    public void eliminarFoto(String email, Long id) {
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        if (!DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException("La publicación no está disponible");
        }
        publicacion.setFotoUrl(null);
        publicacionMaterialRepositorio.save(publicacion);
    }

    private boolean contiene(String valor, String textoBuscado) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(textoBuscado);
    }

    private boolean cumpleCantidadMinima(PublicacionMaterial publicacion, BigDecimal minKg) {
        if (minKg == null) {
            return true;
        }
        BigDecimal kilos =
                switch (publicacion.getUnidadMedida()) {
                    case "kg" -> publicacion.getCantidad();
                    case "g" -> publicacion.getCantidad().movePointLeft(3);
                    case "t" -> publicacion.getCantidad().movePointRight(3);
                    default -> null;
                };
        return kilos != null && kilos.compareTo(minKg) >= 0;
    }

    // Solo el generador propietario puede editar, cancelar o cambiar la foto (US 01-EP1 criterio
    // 13)
    private PublicacionMaterial obtenerPublicacionPropia(String email, Long id) {
        PublicacionMaterial publicacion =
                publicacionMaterialRepositorio
                        .buscarParaActualizar(id)
                        .orElseThrow(() -> new NoSuchElementException("Publicación no encontrada"));
        if (!publicacion.getGenerador().getEmail().equals(email)) {
            throw new AccessDeniedException(
                    "Solo el generador propietario puede modificar la publicación");
        }
        return publicacion;
    }

    // Reglas comunes de creacion y edicion (US 01-EP1, 02-EP1, 04-EP1, 06-EP1)
    private void validarCampos(
            BigDecimal cantidad,
            String direccion,
            LocalDate fechaDisponibilidad,
            String descripcion,
            List<String> errores) {
        if (cantidad == null) {
            errores.add("cantidad: es obligatoria");
        } else if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("cantidad: La cantidad debe ser mayor a cero");
        }
        if (cantidad != null
                && (cantidad.precision() - cantidad.scale() > 6 || cantidad.scale() > 2)) {
            errores.add("cantidad: máximo 6 enteros y 2 decimales");
        }
        if (direccion != null && direccion.length() > 255) {
            errores.add("direccion: máximo 255 caracteres");
        }
        if (direccion == null || direccion.isBlank()) {
            errores.add("direccion: es obligatoria");
        }
        if (fechaDisponibilidad == null) {
            errores.add("fechaDisponibilidad: es obligatoria");
        } else if (fechaDisponibilidad.isBefore(LocalDate.now())) {
            errores.add("fechaDisponibilidad: no puede ser anterior al día actual");
        }
        if (descripcion != null && descripcion.length() > DESCRIPCION_MAX) {
            errores.add("descripcion: no puede superar los " + DESCRIPCION_MAX + " caracteres");
        } else if (descripcion != null && descripcion.matches(PATRON_ETIQUETA)) {
            errores.add("descripcion: no admite contenido HTML");
        }
    }

    private void validarPago(BigDecimal monto, String metodo, List<String> errores) {
        if (monto != null
                && (monto.signum() <= 0
                        || monto.scale() > 2
                        || monto.precision() - monto.scale() > 8)) {
            errores.add("montoPago: debe ser positivo, máximo 8 enteros y 2 decimales");
        }
        if (metodo != null && !UsuarioServiceImpl.METODOS_PAGO.contains(metodo)) {
            errores.add("metodoPago: no admitido");
        }
        if (metodo != null && monto == null) {
            errores.add("montoPago: obligatorio si se indica método");
        }
    }

    private void validarCoordenadas(BigDecimal lat, BigDecimal lon, List<String> errores) {
        if (lat == null
                || lon == null
                || lat.abs().compareTo(BigDecimal.valueOf(90)) > 0
                || lon.abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            errores.add("coordenadas: fuera de rango o incompletas");
        }
    }

    private void validarUnidad(String unidad) {
        if (!List.of("kg", "g", "t", "unidad").contains(unidad)) {
            throw new IllegalArgumentException("unidadMedida: debe ser kg, g, t o unidad");
        }
    }
}
