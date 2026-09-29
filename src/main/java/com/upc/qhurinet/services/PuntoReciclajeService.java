package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.PuntoReciclajeDTO;
import com.upc.qhurinet.entities.PuntoReciclaje;

import java.util.List;

public interface PuntoReciclajeService {
    public List<PuntoReciclajeDTO> listar(List<Integer> material, String type, String q);   // END-34
    public PuntoReciclajeDTO buscarPorId(Long id);                                          // END-35

    // Usado por RutaRecoleccionService (404 si no existe)
    public PuntoReciclaje obtenerPunto(Long id);
}
