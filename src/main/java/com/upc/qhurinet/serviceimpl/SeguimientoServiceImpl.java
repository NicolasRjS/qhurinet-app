package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.SeguimientoDTO;
import com.upc.qhurinet.dtos.UbicacionDTO;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.services.SeguimientoService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SeguimientoServiceImpl implements SeguimientoService {

    // Solo la ultima posicion y solo en memoria; no se conserva historial.
    private final Map<Long, Posicion> posiciones = new ConcurrentHashMap<>();

    @Value("${seguimiento.caducidad-segundos:120}")
    private long caducidad;

    @Value("${rutas.osrm.url}")
    private String osrmUrl;

    private final RestClient cliente = crearCliente();

    @Override
    public void actualizar(Long id, UbicacionDTO datos) {
        if (datos == null
                || datos.getLatitud() == null
                || datos.getLongitud() == null
                || datos.getLatitud().abs().compareTo(BigDecimal.valueOf(90)) > 0
                || datos.getLongitud().abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("coordenadas: fuera de rango o incompletas");
        }
        posiciones.put(id, new Posicion(datos));
    }

    @Scheduled(fixedDelay = 30000)
    public void depurar() {
        posiciones
                .entrySet()
                .removeIf(
                        e -> e.getValue().recibida.plusSeconds(caducidad).isBefore(Instant.now()));
    }

    @Override
    public void eliminar(Long id) {
        posiciones.remove(id);
    }

    @Override
    public SeguimientoDTO obtener(SolicitudRecoleccion solicitud) {
        Posicion p = posiciones.get(solicitud.getId());
        if (p == null || p.recibida.plusSeconds(caducidad).isBefore(Instant.now())) {
            throw new IllegalStateException("No hay una ubicación reciente del recolector");
        }
        var destino = solicitud.getPublicacion();
        SeguimientoDTO dto =
                new SeguimientoDTO(
                        solicitud.getId(),
                        solicitud.getEstado(),
                        solicitud.getFechaCoordinada(),
                        solicitud.getRecolector().getNombreCompleto(),
                        destino.getDireccion(),
                        destino.getLatitud(),
                        destino.getLongitud(),
                        p.latitud,
                        p.longitud,
                        null);
        dto.setFechaActualizacion(LocalDateTime.ofInstant(p.recibida, ZoneId.of("America/Lima")));
        String coordenadas =
                p.longitud
                        + ","
                        + p.latitud
                        + ";"
                        + destino.getLongitud()
                        + ","
                        + destino.getLatitud();
        try {
            Map<?, ?> respuesta =
                    cliente.get()
                            .uri(osrmUrl + "/route/v1/driving/" + coordenadas + "?overview=false")
                            .retrieve()
                            .body(Map.class);
            if (respuesta != null
                    && "Ok".equals(respuesta.get("code"))
                    && respuesta.get("routes") instanceof List<?> rutas
                    && !rutas.isEmpty()
                    && rutas.getFirst() instanceof Map<?, ?> ruta
                    && ruta.get("duration") instanceof Number n
                    && Double.isFinite(n.doubleValue())
                    && n.doubleValue() >= 0) {
                dto.setMinutosEstimados((int) Math.ceil(n.doubleValue() / 60));
            }
        } catch (RestClientException ex) {
            // Sin estimacion disponible, conservar la posicion real; nunca inventar un ETA.
        }
        return dto;
    }

    private RestClient crearCliente() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(5));
        f.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(f).build();
    }

    private static class Posicion {

        BigDecimal latitud;

        BigDecimal longitud;

        Instant recibida;

        Posicion(UbicacionDTO datos) {
            latitud = datos.getLatitud();
            longitud = datos.getLongitud();
            recibida = Instant.now();
        }
    }
}
