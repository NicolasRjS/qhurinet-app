package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.HistorialDTO;
import com.upc.qhurinet.dtos.ReporteMaterialDTO;
import com.upc.qhurinet.dtos.ResumenReporteDTO;

import java.time.LocalDate;
import java.util.List;

// Reportes de la Epica 5. No tiene entidad propia: consulta solicitudes y publicaciones
public interface ReporteService {
    public ResumenReporteDTO obtenerResumen(String email, LocalDate desde, LocalDate hasta);               // END-41
    public List<ReporteMaterialDTO> obtenerPorMaterial(String email, LocalDate desde, LocalDate hasta);   // END-42
    public List<HistorialDTO> obtenerHistorial(String email, String tipo, LocalDate desde, LocalDate hasta); // END-43
    public byte[] exportar(String email, String formato, LocalDate desde, LocalDate hasta);               // END-44
}
