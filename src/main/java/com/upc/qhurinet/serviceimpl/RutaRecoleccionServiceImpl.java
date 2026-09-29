package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.PuntoReciclaje;
import com.upc.qhurinet.entities.RutaParada;
import com.upc.qhurinet.entities.RutaRecoleccion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.RutaRecoleccionRepositorio;
import com.upc.qhurinet.services.PuntoReciclajeService;
import com.upc.qhurinet.services.RutaRecoleccionService;
import com.upc.qhurinet.services.UsuarioService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RutaRecoleccionServiceImpl implements RutaRecoleccionService {
    private static final int PARADAS_MIN = 2;    // US 21-EP3
    private static final int PARADAS_MAX = 10;   // limite del servicio externo de rutas
    private static final int NOMBRE_MAX = 150;   // rutas_recoleccion.nombre varchar(150)

    @Autowired
    private RutaRecoleccionRepositorio rutaRecoleccionRepositorio;
    @Autowired
    private PuntoReciclajeService puntoReciclajeService;
    @Autowired
    private UsuarioService usuarioService;

    @Override
    public RutaOptimizadaDTO optimizar(String email, OptimizarRutaDTO optimizarRutaDTO) {
        List<Long> puntosIds = optimizarRutaDTO.getPuntosIds();
        if (puntosIds == null || puntosIds.size() < PARADAS_MIN) {
            throw new IllegalArgumentException("puntosIds: Selecciona al menos dos puntos para generar una ruta");
        }
        if (puntosIds.size() > PARADAS_MAX) {
            throw new IllegalArgumentException("puntosIds: el máximo es de " + PARADAS_MAX + " paradas");
        }
        if (optimizarRutaDTO.getLatitudOrigen() == null || optimizarRutaDTO.getLongitudOrigen() == null) {
            throw new IllegalArgumentException("latitudOrigen y longitudOrigen: son obligatorias");
        }
        // Las coordenadas se toman de la base, no del cliente (404 si algun punto no existe)
        List<PuntoReciclaje> puntos = puntosIds.stream().map(puntoReciclajeService::obtenerPunto).toList();
        // PENDIENTE (servicio externo): enviar origen y puntos al proveedor de rutas y devolver el orden,
        // la distancia y el tiempo. Si no responde, 503 (US 21-EP3). La ruta no se guarda aqui.
        throw new UnsupportedOperationException("END-36 pendiente: falta integrar el servicio externo de rutas ("
                + puntos.size() + " puntos validados)");
    }

    // La ruta y sus paradas se guardan en una sola transaccion por el cascade de RutaRecoleccion.paradas
    @Transactional
    @Override
    public RutaDTO crear(String email, CrearRutaDTO crearRutaDTO) {
        List<String> errores = new ArrayList<>();
        if (crearRutaDTO.getNombre() == null || crearRutaDTO.getNombre().isBlank()) {
            errores.add("nombre: es obligatorio");
        } else if (crearRutaDTO.getNombre().trim().length() > NOMBRE_MAX) {
            errores.add("nombre: no puede superar los " + NOMBRE_MAX + " caracteres");
        }
        List<ParadaRutaDTO> paradas = crearRutaDTO.getParadas();
        if (paradas == null || paradas.isEmpty()) {
            errores.add("paradas: la ruta debe tener al menos una parada");
        } else {
            Set<Integer> ordenes = new HashSet<>();
            Set<Long> puntos = new HashSet<>();
            for (ParadaRutaDTO parada : paradas) {
                if (parada.getPuntoReciclajeId() == null || parada.getOrden() == null) {
                    errores.add("paradas: cada parada requiere puntoReciclajeId y orden");
                    break;
                }
                if (!ordenes.add(parada.getOrden())) {
                    errores.add("paradas: el orden " + parada.getOrden() + " está repetido");
                }
                if (!puntos.add(parada.getPuntoReciclajeId())) {
                    errores.add("paradas: el punto " + parada.getPuntoReciclajeId() + " está repetido");
                }
            }
        }
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", errores));
        }

        RutaRecoleccion ruta = new RutaRecoleccion();
        ruta.setRecolector(usuarioService.obtenerUsuario(email));
        ruta.setNombre(crearRutaDTO.getNombre().trim());
        ruta.setDescripcion(crearRutaDTO.getDescripcion());
        ruta.setFechaRuta(crearRutaDTO.getFechaRuta());
        ruta.setDistanciaTotalKm(crearRutaDTO.getDistanciaTotalKm());
        ruta.setTiempoEstimadoMin(crearRutaDTO.getTiempoEstimadoMin());
        for (ParadaRutaDTO paradaDTO : paradas) {
            RutaParada parada = new RutaParada();
            parada.setRuta(ruta);
            parada.setPuntoReciclaje(puntoReciclajeService.obtenerPunto(paradaDTO.getPuntoReciclajeId()));
            parada.setOrden(paradaDTO.getOrden());
            ruta.getParadas().add(parada);
        }
        return aDTO(rutaRecoleccionRepositorio.save(ruta));
    }

    @Override
    public List<RutaResumenDTO> listarMisRutas(String email) {
        Usuario recolector = usuarioService.obtenerUsuario(email);
        // PENDIENTE (query): rutas del recolector con su numero de paradas, ordenadas por fecha_creacion DESC (END-38)
        throw new UnsupportedOperationException("END-38 pendiente: falta la consulta de rutas del recolector " + recolector.getId());
    }

    @Override
    public RutaDTO buscarPorId(String email, Long id) {
        return aDTO(obtenerRutaPropia(email, id));
    }

    // Las paradas se eliminan en cascada (orphanRemoval)
    @Transactional
    @Override
    public void eliminar(String email, Long id) {
        rutaRecoleccionRepositorio.delete(obtenerRutaPropia(email, id));
    }

    // Cada recolector solo accede a sus propias rutas (US 23-EP3 criterio 7)
    private RutaRecoleccion obtenerRutaPropia(String email, Long id) {
        RutaRecoleccion ruta = rutaRecoleccionRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada"));
        if (!ruta.getRecolector().getEmail().equals(email)) {
            throw new AccessDeniedException("La ruta no pertenece al usuario");
        }
        return ruta;
    }

    // Mapeo manual: la clave compuesta de RutaParada confunde a ModelMapper
    private RutaDTO aDTO(RutaRecoleccion ruta) {
        List<ParadaRutaDTO> paradas = ruta.getParadas()
                .stream()
                .sorted(Comparator.comparing(RutaParada::getOrden))
                .map(parada -> new ParadaRutaDTO(parada.getPuntoReciclaje().getId(), parada.getOrden(),
                        parada.getPuntoReciclaje().getNombre(), parada.getPuntoReciclaje().getDireccion(),
                        parada.getPuntoReciclaje().getLatitud(), parada.getPuntoReciclaje().getLongitud()))
                .toList();
        return new RutaDTO(ruta.getId(), ruta.getNombre(), ruta.getDescripcion(), ruta.getFechaRuta(),
                ruta.getDistanciaTotalKm(), ruta.getTiempoEstimadoMin(), ruta.getFechaCreacion(), paradas);
    }
}
