package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.CrearRutaDTO;
import com.upc.qhurinet.dtos.OptimizarRutaDTO;
import com.upc.qhurinet.dtos.ParadaRutaDTO;
import com.upc.qhurinet.dtos.RutaDTO;
import com.upc.qhurinet.dtos.RutaOptimizadaDTO;
import com.upc.qhurinet.dtos.RutaResumenDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
public class RutaRecoleccionServiceImpl implements RutaRecoleccionService {
    private static final int PARADAS_MIN = 2;    // US 21-EP3
    private static final int PARADAS_MAX = 10;   // limite del servicio externo de rutas
    private static final int NOMBRE_MAX = 150;   // rutas_recoleccion.nombre varchar(150)

    @Value("${rutas.osrm.url}")
    private String rutasOsrmUrl;

    // RestClient se usa por la integracion aprobada con OSRM.
    private final RestClient restClient = crearRestClient();

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
        if (puntosIds.stream().anyMatch(Objects::isNull) || new HashSet<>(puntosIds).size() != puntosIds.size()) {
            throw new IllegalArgumentException("puntosIds: no admite nulos ni repetidos");
        }
        if (optimizarRutaDTO.getLatitudOrigen().abs().compareTo(BigDecimal.valueOf(90)) > 0
                || optimizarRutaDTO.getLongitudOrigen().abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("coordenadas: fuera de rango");
        }
        // Las coordenadas se toman de la base, no del cliente (404 si algun punto no existe)
        List<PuntoReciclaje> puntos = puntosIds.stream().map(puntoReciclajeService::obtenerPunto).toList();

        StringBuilder coordenadas = new StringBuilder();
        coordenadas.append(optimizarRutaDTO.getLongitudOrigen()).append(',')
                .append(optimizarRutaDTO.getLatitudOrigen());
        for (PuntoReciclaje punto : puntos) {
            coordenadas.append(';').append(punto.getLongitud()).append(',').append(punto.getLatitud());
        }
        String url = rutasOsrmUrl.replaceAll("/+$", "") + "/trip/v1/driving/" + coordenadas
                + "?source=first&roundtrip=false&destination=any&overview=false";

        Map<?, ?> respuesta = restClient.get()
                .uri(url)
                .retrieve()
                .body(Map.class);
        if (respuesta == null || !"Ok".equals(respuesta.get("code"))) {
            throw new RestClientException("El servicio de rutas no está disponible");
        }

        Object waypointsRespuesta = respuesta.get("waypoints");
        Object viajesRespuesta = respuesta.get("trips");
        if (!(waypointsRespuesta instanceof List) || !(viajesRespuesta instanceof List)) {
            throw new RestClientException("El servicio de rutas no está disponible");
        }
        List<?> waypoints = (List<?>) waypointsRespuesta;
        List<?> viajes = (List<?>) viajesRespuesta;
        if (waypoints.size() != puntos.size() + 1 || viajes.isEmpty() || !(viajes.get(0) instanceof Map)) {
            throw new RestClientException("El servicio de rutas no está disponible");
        }
        Map<?, ?> viaje = (Map<?, ?>) viajes.get(0);
        Object distanciaRespuesta = viaje.get("distance");
        Object duracionRespuesta = viaje.get("duration");
        if (!(distanciaRespuesta instanceof Number) || !(duracionRespuesta instanceof Number)) {
            throw new RestClientException("El servicio de rutas no está disponible");
        }
        Number distancia = (Number) distanciaRespuesta;
        Number duracion = (Number) duracionRespuesta;

        List<ParadaRutaDTO> paradas = new ArrayList<>();
        for (int i = 1; i < waypoints.size(); i++) {
            if (!(waypoints.get(i) instanceof Map)) {
                throw new RestClientException("El servicio de rutas no está disponible");
            }
            Map<?, ?> waypoint = (Map<?, ?>) waypoints.get(i);
            Object ordenRespuesta = waypoint.get("waypoint_index");
            if (!(ordenRespuesta instanceof Number)) {
                throw new RestClientException("El servicio de rutas no está disponible");
            }
            PuntoReciclaje punto = puntos.get(i - 1);
            paradas.add(new ParadaRutaDTO(punto.getId(), ((Number) ordenRespuesta).intValue(), punto.getNombre(),
                    punto.getDireccion(), punto.getLatitud(), punto.getLongitud()));
        }
        if (paradas.stream().map(ParadaRutaDTO::getOrden).distinct().count() != puntos.size()
                || paradas.stream().anyMatch(p -> p.getOrden() < 1 || p.getOrden() > puntos.size())
                || !Double.isFinite(distancia.doubleValue()) || distancia.doubleValue() < 0
                || !Double.isFinite(duracion.doubleValue()) || duracion.doubleValue() < 0) {
            throw new RestClientException("Respuesta de rutas inválida");
        }
        paradas.sort(Comparator.comparing(ParadaRutaDTO::getOrden));

        BigDecimal distanciaKm = BigDecimal.valueOf(distancia.doubleValue())
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        int tiempoMinutos = (int) Math.round(duracion.doubleValue() / 60);
        return new RutaOptimizadaDTO(paradas, distanciaKm, tiempoMinutos);
    }

    private RestClient crearRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(requestFactory).build();
    }

    // La ruta y sus paradas se guardan en una sola transaccion por el cascade de RutaRecoleccion.paradas
    @Transactional
    @Override
    public RutaDTO crear(String email, CrearRutaDTO crearRutaDTO) {
        List<String> errores = new ArrayList<>();
        BigDecimal distancia = crearRutaDTO.getDistanciaTotalKm();
        Integer tiempo = crearRutaDTO.getTiempoEstimadoMin();
        if (distancia == null || distancia.signum() < 0 || distancia.scale() > 2 || distancia.precision() - distancia.scale() > 4) {
            errores.add("distanciaTotalKm: obligatoria, de 0 a 9999.99");
        }
        if (tiempo == null || tiempo < 0) errores.add("tiempoEstimadoMin: obligatorio y no negativo");
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
                if (parada == null || parada.getPuntoReciclajeId() == null || parada.getOrden() == null || parada.getOrden() < 1) {
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
        return rutaRecoleccionRepositorio.findByRecolector_IdOrderByFechaCreacionDesc(recolector.getId())
                .stream()
                .map(ruta -> new RutaResumenDTO(ruta.getId(), ruta.getNombre(), ruta.getDescripcion(),
                        ruta.getDistanciaTotalKm(), ruta.getTiempoEstimadoMin(), (long) ruta.getParadas().size()))
                .toList();
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
