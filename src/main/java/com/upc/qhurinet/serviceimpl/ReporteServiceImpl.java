package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.HistorialDTO;
import com.upc.qhurinet.dtos.ReporteMaterialDTO;
import com.upc.qhurinet.dtos.ResumenReporteDTO;
import com.upc.qhurinet.entities.Usuario;
import com.upc.qhurinet.repositories.SolicitudRecoleccionRepositorio;
import com.upc.qhurinet.services.ReporteService;
import com.upc.qhurinet.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        // PENDIENTE (query): SUM(cantidad), COUNT y AVG(calificacion) de solicitudes 'ejecutada' del usuario
        // en el periodo (END-41). Falta definir como se calcula el cumplimiento.
        throw new UnsupportedOperationException("END-41 pendiente: falta la consulta del resumen del usuario " + usuario.getId());
    }

    @Override
    public List<ReporteMaterialDTO> obtenerPorMaterial(String email, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        // PENDIENTE (query): kilos y recolecciones 'ejecutada' agrupados por categoria, ordenados por kilos DESC (END-42)
        throw new UnsupportedOperationException("END-42 pendiente: falta la consulta por material del usuario " + usuario.getId());
    }

    @Override
    public List<HistorialDTO> obtenerHistorial(String email, String tipo, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        Usuario usuario = usuarioService.obtenerUsuario(email);
        // PENDIENTE (query): solicitudes 'ejecutada' donde el usuario es recolector o generador,
        // ordenadas por fecha_ejecucion DESC (END-43)
        throw new UnsupportedOperationException("END-43 pendiente: falta la consulta del historial del usuario " + usuario.getId());
    }

    @Override
    public byte[] exportar(String email, String formato, LocalDate desde, LocalDate hasta) {
        if (formato == null || !FORMATOS.contains(formato)) {
            throw new IllegalArgumentException("formato: debe ser pdf o csv");
        }
        validarPeriodo(desde, hasta);
        // PENDIENTE (query + libreria): reutiliza la consulta de END-43 y genera el archivo (END-44)
        throw new UnsupportedOperationException("END-44 pendiente: falta la consulta del historial y la generación del archivo");
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("desde: no puede ser posterior a hasta");
        }
    }
}
