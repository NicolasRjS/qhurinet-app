package com.upc.qhurinet.controllers;

import com.upc.qhurinet.dtos.HistorialDTO;
import com.upc.qhurinet.dtos.ReporteMaterialDTO;
import com.upc.qhurinet.dtos.ResumenReporteDTO;
import com.upc.qhurinet.services.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

// Reportes e historial (END-41 a END-44). Fechas en formato yyyy-MM-dd
@RestController
@CrossOrigin(origins = "${ip.frontend}", allowCredentials = "true", exposedHeaders = "Authorization")
@RequestMapping("/api/v1")
public class ReporteController {
    @Autowired
    private ReporteService reporteService;

    // END-41
    @GetMapping("/reports/summary")
    public ResponseEntity<ResumenReporteDTO> obtenerResumen(
            @RequestParam(value = "desde", required = false) LocalDate desde,
            @RequestParam(value = "hasta", required = false) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.obtenerResumen(emailAutenticado(), desde, hasta));
    }

    // END-42
    @GetMapping("/reports/materials")
    public ResponseEntity<List<ReporteMaterialDTO>> obtenerPorMaterial(
            @RequestParam(value = "desde", required = false) LocalDate desde,
            @RequestParam(value = "hasta", required = false) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.obtenerPorMaterial(emailAutenticado(), desde, hasta));
    }

    // END-43
    @GetMapping("/history")
    public ResponseEntity<List<HistorialDTO>> obtenerHistorial(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "desde", required = false) LocalDate desde,
            @RequestParam(value = "hasta", required = false) LocalDate hasta) {
        return ResponseEntity.ok(reporteService.obtenerHistorial(emailAutenticado(), tipo, desde, hasta));
    }

    // END-44: archivo descargable (formato = pdf | csv)
    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> exportar(
            @RequestParam(value = "formato", required = false) String formato,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "desde", required = false) LocalDate desde,
            @RequestParam(value = "hasta", required = false) LocalDate hasta) {
        byte[] archivo = reporteService.exportar(emailAutenticado(), formato, tipo, desde, hasta);
        formato = formato.trim().toLowerCase(Locale.ROOT);
        String tipoContenido = "pdf".equals(formato) ? "application/pdf" : "text/csv;charset=UTF-8";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(tipoContenido))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial." + formato)
                .body(archivo);
    }

    private String emailAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
