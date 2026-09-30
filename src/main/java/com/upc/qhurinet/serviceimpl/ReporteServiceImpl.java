package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.HistorialDTO;
import com.upc.qhurinet.dtos.ReporteMaterialDTO;
import com.upc.qhurinet.dtos.ResumenReporteDTO;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.repositories.SolicitudRecoleccionRepositorio;
import com.upc.qhurinet.services.ReporteService;
import com.upc.qhurinet.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {
    private static final List<String> FORMATOS = List.of("pdf", "csv");

    @Autowired
    private SolicitudRecoleccionRepositorio solicitudRecoleccionRepositorio;
    @Autowired
    private UsuarioService usuarioService;

    @Override
    public ResumenReporteDTO obtenerResumen(String email, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        List<SolicitudRecoleccion> solicitudes = obtenerEjecutadasDelPeriodo(usuario, desde, hasta);
        BigDecimal kilos = solicitudes.stream()
                .map(solicitud -> solicitud.getPublicacion().getCantidad())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<SolicitudRecoleccion> calificadas = solicitudes.stream()
                .filter(solicitud -> solicitud.getCalificacionRecolector() != null)
                .toList();
        BigDecimal promedio = calificadas.isEmpty() ? null : calificadas.stream()
                .map(solicitud -> BigDecimal.valueOf(solicitud.getCalificacionRecolector()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(calificadas.size()), 2, RoundingMode.HALF_UP);
        // Cumplimiento no tiene una formula definida en US 34-EP5.
        return new ResumenReporteDTO(kilos, (long) solicitudes.size(), promedio, null);
    }

    @Override
    public List<ReporteMaterialDTO> obtenerPorMaterial(String email, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        List<SolicitudRecoleccion> solicitudes = obtenerEjecutadasDelPeriodo(usuario, desde, hasta);
        Map<String, List<SolicitudRecoleccion>> porMaterial = solicitudes.stream()
                .collect(Collectors.groupingBy(solicitud -> solicitud.getPublicacion().getCategoriaMaterial().getNombre()));
        return porMaterial.entrySet().stream()
                .map(material -> new ReporteMaterialDTO(material.getKey(), material.getValue().stream()
                        .map(solicitud -> solicitud.getPublicacion().getCantidad())
                        .reduce(BigDecimal.ZERO, BigDecimal::add), (long) material.getValue().size()))
                .sorted(Comparator.comparing(ReporteMaterialDTO::getKilos).reversed())
                .toList();
    }

    @Override
    public List<HistorialDTO> obtenerHistorial(String email, String tipo, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        // PENDIENTE (definicion): END-43 y US 33 piden ejecutadas; US 35 tambien exige canceladas.
        // El parametro tipo no tiene valores definidos en las historias.
        throw new UnsupportedOperationException("END-43 pendiente: confirmar si el historial incluye canceladas (US 35-EP5) "
                + "o solo ejecutadas (END-43 y US 33-EP5) para el usuario " + usuario.getId());
    }

    @Override
    public byte[] exportar(String email, String formato, LocalDate desde, LocalDate hasta) {
        if (formato == null || !FORMATOS.contains(formato)) {
            throw new IllegalArgumentException("formato: debe ser pdf o csv");
        }
        validarPeriodo(desde, hasta);
        usuarioService.obtenerUsuario(email);
        if ("pdf".equals(formato)) {
            // PENDIENTE (libreria): falta acordar una libreria PDF con el equipo.
            throw new UnsupportedOperationException("END-44 pendiente: la exportación PDF requiere una librería aprobada por el equipo");
        }
        // PENDIENTE (definicion): el CSV depende del alcance del historial de END-43.
        // US 36 pide estado y monto; HistorialDTO no los expone y END-43 no los especifica.
        throw new UnsupportedOperationException("END-44 pendiente: confirmar el alcance de END-43 y los campos estado y monto "
                + "del historial antes de generar el CSV");
    }

    private List<SolicitudRecoleccion> obtenerEjecutadasDelPeriodo(Usuario usuario, LocalDate desde, LocalDate hasta) {
        // US 34-EP5: sin fechas se consulta el mes en curso; un extremo omitido queda abierto.
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = desde == null && hasta == null ? hoy.withDayOfMonth(1) : desde;
        LocalDate fin = desde == null && hasta == null ? hoy.withDayOfMonth(hoy.lengthOfMonth()) : hasta;
        List<SolicitudRecoleccion> solicitudes;
        if (RolServiceImpl.GENERADOR.equals(usuario.getRol().getNombre())) {
            solicitudes = solicitudRecoleccionRepositorio.findByPublicacion_Generador_IdAndEstado(
                    usuario.getId(), SolicitudRecoleccionServiceImpl.EJECUTADA);
        } else if (RolServiceImpl.RECOLECTOR.equals(usuario.getRol().getNombre())) {
            solicitudes = solicitudRecoleccionRepositorio.findByRecolector_IdAndEstado(
                    usuario.getId(), SolicitudRecoleccionServiceImpl.EJECUTADA);
        } else {
            solicitudes = List.of();
        }
        return solicitudes.stream()
                .filter(solicitud -> (inicio == null && fin == null)
                        || (solicitud.getFechaEjecucion() != null
                        && (inicio == null || !solicitud.getFechaEjecucion().toLocalDate().isBefore(inicio))
                        && (fin == null || !solicitud.getFechaEjecucion().toLocalDate().isAfter(fin))))
                .toList();
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("desde: no puede ser posterior a hasta");
        }
    }
}
