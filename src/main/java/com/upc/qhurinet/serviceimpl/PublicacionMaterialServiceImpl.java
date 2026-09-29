package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.CategoriaMaterial;
import com.upc.qhurinet.entities.PublicacionMaterial;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.PublicacionMaterialRepositorio;
import com.upc.qhurinet.services.AlmacenamientoService;
import com.upc.qhurinet.services.CategoriaMaterialService;
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
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PublicacionMaterialServiceImpl implements PublicacionMaterialService {
    // Valores admitidos en publicaciones_material.estado
    public static final String DISPONIBLE = "disponible";
    public static final String RESERVADO = "reservado";
    public static final String RECOLECTADO = "recolectado";
    public static final String CANCELADO = "cancelado";

    private static final int DESCRIPCION_MAX = 200;                 // US 02-EP1
    private static final String PATRON_ETIQUETA = "(?s).*<[^>]*>.*"; // US 02-EP1: sin HTML
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

    @Transactional
    @Override
    public PublicacionDTO crear(String email, CrearPublicacionDTO crearPublicacionDTO) {
        List<String> errores = new ArrayList<>();
        if (crearPublicacionDTO.getCategoriaMaterialId() == null) {
            errores.add("categoriaMaterialId: es obligatorio");
        }
        validarCampos(crearPublicacionDTO.getCantidad(), crearPublicacionDTO.getDireccion(),
                crearPublicacionDTO.getFechaDisponibilidad(), crearPublicacionDTO.getDescripcion(), errores);
        if (crearPublicacionDTO.getLatitud() == null || crearPublicacionDTO.getLongitud() == null) {
            errores.add("latitud y longitud: son obligatorias");
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }

        Usuario generador = usuarioService.obtenerUsuario(email);
        if (!UsuarioServiceImpl.ESTADO_ACTIVO.equals(generador.getEstado())) {
            throw new AccessDeniedException("La cuenta debe estar activa para publicar material");
        }
        CategoriaMaterial categoria = categoriaMaterialService.obtenerCategoria(crearPublicacionDTO.getCategoriaMaterialId());

        // El generador sale del token; el estado y la fecha de publicacion los asigna el servidor
        PublicacionMaterial publicacion = new PublicacionMaterial();
        publicacion.setGenerador(generador);
        publicacion.setCategoriaMaterial(categoria);
        publicacion.setCantidad(crearPublicacionDTO.getCantidad());
        publicacion.setUnidadMedida(crearPublicacionDTO.getUnidadMedida() == null || crearPublicacionDTO.getUnidadMedida().isBlank()
                ? categoria.getUnidadMedidaDefault() : crearPublicacionDTO.getUnidadMedida());
        publicacion.setDescripcion(crearPublicacionDTO.getDescripcion());
        publicacion.setFotoUrl(crearPublicacionDTO.getFotoUrl());
        publicacion.setDireccion(crearPublicacionDTO.getDireccion().trim());
        publicacion.setDistrito(crearPublicacionDTO.getDistrito());
        publicacion.setLatitud(crearPublicacionDTO.getLatitud());
        publicacion.setLongitud(crearPublicacionDTO.getLongitud());
        publicacion.setFechaDisponibilidad(crearPublicacionDTO.getFechaDisponibilidad());
        return modelMapper.map(publicacionMaterialRepositorio.save(publicacion), PublicacionDTO.class);
    }

    @Override
    public PublicacionDTO buscarPorId(Long id) {
        return modelMapper.map(obtenerPublicacion(id), PublicacionDTO.class);
    }

    @Override
    public List<PublicacionDTO> listarParaMapa(Integer material, String distrito, String q,
                                               BigDecimal minKg, LocalDate fecha, String estado) {
        // PENDIENTE (query): publicaciones 'disponible' filtradas por categoria, distrito/direccion (q),
        // cantidad minima y fecha de disponibilidad (END-16). Luego mapear a PublicacionDTO.
        throw new UnsupportedOperationException("END-16 pendiente: falta la consulta de publicaciones del mapa");
    }

    @Override
    public List<MiPublicacionDTO> listarMisPublicaciones(String email, String estado) {
        Usuario generador = usuarioService.obtenerUsuario(email);
        // PENDIENTE (query): publicaciones del generador con el reciclador y la fecha coordinada de su solicitud,
        // ordenadas por fecha_publicacion DESC. estado = activos | completados | cancelados (END-17).
        throw new UnsupportedOperationException("END-17 pendiente: falta la consulta de publicaciones del generador " + generador.getId());
    }

    @Transactional
    @Override
    public PublicacionDTO editar(String email, Long id, EditarPublicacionDTO editarPublicacionDTO) {
        List<String> errores = new ArrayList<>();
        validarCampos(editarPublicacionDTO.getCantidad(), editarPublicacionDTO.getDireccion(),
                editarPublicacionDTO.getFechaDisponibilidad(), editarPublicacionDTO.getDescripcion(), errores);
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        if (!DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException("Solo se puede editar una publicación disponible; para cambiar el horario use la reprogramación");
        }
        publicacion.setCantidad(editarPublicacionDTO.getCantidad());
        publicacion.setDescripcion(editarPublicacionDTO.getDescripcion());
        publicacion.setDireccion(editarPublicacionDTO.getDireccion().trim());
        publicacion.setFechaDisponibilidad(editarPublicacionDTO.getFechaDisponibilidad());
        return modelMapper.map(publicacionMaterialRepositorio.save(publicacion), PublicacionDTO.class);
    }

    // US 09-EP2 criterio 8: el generador cancela su publicacion mientras no haya sido reclamada
    @Transactional
    @Override
    public PublicacionDTO cancelar(String email, Long id) {
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        if (!DISPONIBLE.equals(publicacion.getEstado())) {
            throw new IllegalStateException("Solo se puede cancelar una publicación que no ha sido reclamada");
        }
        publicacion.setEstado(CANCELADO);
        return modelMapper.map(publicacionMaterialRepositorio.save(publicacion), PublicacionDTO.class);
    }

    @Transactional
    @Override
    public FotoPublicacionDTO subirFoto(String email, Long id, MultipartFile archivo) {
        PublicacionMaterial publicacion = obtenerPublicacionPropia(email, id);
        String ruta = almacenamientoService.guardar(archivo, CARPETA_FOTOS, FORMATOS_IMAGEN);
        publicacion.setFotoUrl(ruta);
        publicacionMaterialRepositorio.save(publicacion);
        return new FotoPublicacionDTO(ruta);
    }

    @Override
    public PublicacionMaterial obtenerPublicacion(Long id) {
        return publicacionMaterialRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Publicación no encontrada"));
    }

    // Solo el generador propietario puede editar, cancelar o cambiar la foto (US 01-EP1 criterio 13)
    private PublicacionMaterial obtenerPublicacionPropia(String email, Long id) {
        PublicacionMaterial publicacion = obtenerPublicacion(id);
        if (!publicacion.getGenerador().getEmail().equals(email)) {
            throw new AccessDeniedException("Solo el generador propietario puede modificar la publicación");
        }
        return publicacion;
    }

    // Reglas comunes de creacion y edicion (US 01-EP1, 02-EP1, 04-EP1, 06-EP1)
    private void validarCampos(BigDecimal cantidad, String direccion, LocalDate fechaDisponibilidad,
                               String descripcion, List<String> errores) {
        if (cantidad == null) {
            errores.add("cantidad: es obligatoria");
        } else if (cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            errores.add("cantidad: La cantidad debe ser mayor a cero");
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
}
