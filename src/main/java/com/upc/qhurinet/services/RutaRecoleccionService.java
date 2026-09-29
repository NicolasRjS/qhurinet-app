package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.*;

import java.util.List;

public interface RutaRecoleccionService {
    public RutaOptimizadaDTO optimizar(String email, OptimizarRutaDTO optimizarRutaDTO);   // END-36
    public RutaDTO crear(String email, CrearRutaDTO crearRutaDTO);                         // END-37
    public List<RutaResumenDTO> listarMisRutas(String email);                              // END-38
    public RutaDTO buscarPorId(String email, Long id);                                     // END-39
    public void eliminar(String email, Long id);                                           // END-40
}
