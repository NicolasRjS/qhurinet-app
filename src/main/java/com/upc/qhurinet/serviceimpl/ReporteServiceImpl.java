package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.HistorialDTO;
import com.upc.qhurinet.dtos.ReporteMaterialDTO;
import com.upc.qhurinet.dtos.ResumenReporteDTO;
import com.upc.qhurinet.entities.SolicitudRecoleccion;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.SolicitudRecoleccionRepositorio;
import com.upc.qhurinet.services.ReporteService;
import com.upc.qhurinet.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {
    private static final List<String> FORMATOS = List.of("pdf", "csv");

    @Autowired
    private SolicitudRecoleccionRepositorio solicitudRecoleccionRepositorio;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private com.upc.qhurinet.services.ReportePdfService reportePdfService;

    @Override
    public ResumenReporteDTO obtenerResumen(String email, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        List<SolicitudRecoleccion> solicitudes = obtenerEjecutadasDelPeriodo(usuario, desde, hasta);
        BigDecimal kilos = solicitudes.stream()
                .map(this::kilos)
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
                .collect(Collectors.groupingBy(s -> s.getPublicacion().getCategoriaMaterial().getNombre()
                        + "|" + s.getPublicacion().getUnidadMedida()));
        return porMaterial.values().stream().map(grupo -> {
            String unidad = grupo.getFirst().getPublicacion().getUnidadMedida();
            BigDecimal cantidad = grupo.stream().map(s -> s.getPublicacion().getCantidad()).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal masa = List.of("kg", "g", "t").contains(unidad)
                    ? grupo.stream().map(this::kilos).reduce(BigDecimal.ZERO, BigDecimal::add) : null;
            return new ReporteMaterialDTO(grupo.getFirst().getPublicacion().getCategoriaMaterial().getNombre(),
                    masa, (long) grupo.size(), cantidad, unidad);
        }).sorted(Comparator.comparing(ReporteMaterialDTO::getKilos, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

    }

    @Override
    public List<HistorialDTO> obtenerHistorial(String email, String tipo, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        return obtenerSolicitudesHistorial(usuario, tipo, desde, hasta)
                .stream()
                .map(solicitud -> convertirHistorial(usuario, solicitud))
                .toList();
    }

    @Override
    public byte[] exportar(String email, String formato, String tipo, LocalDate desde, LocalDate hasta) {
        String formatoNormalizado = formato == null ? "" : formato.trim().toLowerCase();
        if (!FORMATOS.contains(formatoNormalizado)) {
            throw new IllegalArgumentException("formato: debe ser pdf o csv");
        }
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        List<SolicitudRecoleccion> solicitudes = obtenerSolicitudesHistorial(usuario, tipo, desde, hasta);
        if (solicitudes.isEmpty()) {
            throw new IllegalArgumentException("No hay datos para exportar en el periodo");
        }
        if ("pdf".equals(formatoNormalizado)) {
            return reportePdfService.generar(usuario.getNombreCompleto(),
                    (desde == null ? "Inicio" : desde.toString()) + " - " + (hasta == null ? "Actualidad" : hasta.toString()),
                    solicitudes.stream().map(s -> convertirHistorial(usuario, s)).toList());
        }
        return construirCsv(usuario, solicitudes).getBytes(StandardCharsets.UTF_8);
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

    private List<SolicitudRecoleccion> obtenerSolicitudesHistorial(Usuario usuario, String tipo, LocalDate desde, LocalDate hasta) {
        List<String> estados = estadosHistorial(tipo);
        List<SolicitudRecoleccion> solicitudes;
        if (RolServiceImpl.GENERADOR.equals(usuario.getRol().getNombre())) {
            solicitudes = solicitudRecoleccionRepositorio.findByPublicacion_Generador_IdAndEstadoIn(usuario.getId(), estados);
        } else if (RolServiceImpl.RECOLECTOR.equals(usuario.getRol().getNombre())) {
            solicitudes = solicitudRecoleccionRepositorio.findByRecolector_IdAndEstadoIn(usuario.getId(), estados);
        } else {
            solicitudes = List.of();
        }
        return solicitudes.stream()
                .filter(solicitud -> estaEnPeriodo(fechaHistorial(solicitud), desde, hasta))
                .sorted(Comparator.comparing((SolicitudRecoleccion solicitud) -> fechaHistorial(solicitud)).reversed())
                .toList();
    }

    private List<String> estadosHistorial(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return List.of(SolicitudRecoleccionServiceImpl.EJECUTADA, SolicitudRecoleccionServiceImpl.CANCELADA);
        }
        String tipoNormalizado = tipo.trim().toLowerCase();
        if ("ejecutadas".equals(tipoNormalizado)) {
            return List.of(SolicitudRecoleccionServiceImpl.EJECUTADA);
        }
        if ("canceladas".equals(tipoNormalizado)) {
            return List.of(SolicitudRecoleccionServiceImpl.CANCELADA);
        }
        throw new IllegalArgumentException("tipo: debe ser ejecutadas o canceladas");
    }

    private boolean estaEnPeriodo(LocalDateTime fecha, LocalDate desde, LocalDate hasta) {
        return (desde == null || !fecha.toLocalDate().isBefore(desde))
                && (hasta == null || !fecha.toLocalDate().isAfter(hasta));
    }

    private HistorialDTO convertirHistorial(Usuario usuario, SolicitudRecoleccion solicitud) {
        HistorialDTO dto = new HistorialDTO(fechaHistorial(solicitud), nombreMaterial(solicitud),
                solicitud.getPublicacion().getCantidad(), nombreContraparte(usuario, solicitud),
                solicitud.getCalificacionRecolector(), solicitud.getEstado(), solicitud.getMontoPago());
        dto.setSolicitudId(solicitud.getId()); dto.setUnidadMedida(solicitud.getPublicacion().getUnidadMedida());
        return dto;
    }

    private String construirCsv(Usuario usuario, List<SolicitudRecoleccion> solicitudes) {
        StringBuilder csv = new StringBuilder("fecha,material,cantidad,unidad,contraparte,estado,monto\n");
        solicitudes.forEach(solicitud -> csv.append(campoCsv(fechaHistorial(solicitud)))
                .append(",").append(campoCsv(nombreMaterial(solicitud)))
                .append(",").append(campoCsv(solicitud.getPublicacion().getCantidad()))
                .append(",").append(campoCsv(solicitud.getPublicacion().getUnidadMedida()))
                .append(",").append(campoCsv(nombreContraparte(usuario, solicitud)))
                .append(",").append(campoCsv(solicitud.getEstado()))
                .append(",").append(campoCsv(solicitud.getMontoPago()))
                .append("\n"));
        return csv.toString();
    }

    private String campoCsv(Object valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor instanceof BigDecimal ? ((BigDecimal) valor).toPlainString() : valor.toString();
        if (valor instanceof String && texto.stripLeading().matches("(?s)^[=+@\\-].*")) texto = "'" + texto;
        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }

    private LocalDateTime fechaHistorial(SolicitudRecoleccion solicitud) {
        return solicitud.getFechaEjecucion() == null ? solicitud.getFechaSolicitud() : solicitud.getFechaEjecucion();
    }

    private String nombreMaterial(SolicitudRecoleccion solicitud) {
        return solicitud.getPublicacion().getCategoriaMaterial().getNombre();
    }

    private String nombreContraparte(Usuario usuario, SolicitudRecoleccion solicitud) {
        if (solicitud.getRecolector() != null && solicitud.getRecolector().getId().equals(usuario.getId())) {
            return solicitud.getPublicacion().getGenerador().getNombreCompleto();
        }
        return solicitud.getRecolector() == null ? null : solicitud.getRecolector().getNombreCompleto();
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("desde: no puede ser posterior a hasta");
        }
    }
    private BigDecimal kilos(SolicitudRecoleccion s) {
        BigDecimal cantidad = s.getPublicacion().getCantidad();
        return switch (s.getPublicacion().getUnidadMedida()) {
            case "kg" -> cantidad;
            case "g" -> cantidad.movePointLeft(3);
            case "t" -> cantidad.movePointRight(3);
            default -> BigDecimal.ZERO;
        };
    }

}
