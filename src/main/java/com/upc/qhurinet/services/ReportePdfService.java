package com.upc.qhurinet.services;
import com.upc.qhurinet.dtos.HistorialDTO;
import java.util.List;
public interface ReportePdfService {
    byte[] generar(String usuario, String periodo, List<HistorialDTO> registros);
}
