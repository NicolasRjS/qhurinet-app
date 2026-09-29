package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.*;
import com.upc.qhurinet.entities.PublicacionMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PublicacionMaterialService {
    public PublicacionDTO crear(String email, CrearPublicacionDTO crearPublicacionDTO);                    // END-14
    public PublicacionDTO buscarPorId(Long id);                                                           // END-15
    public List<PublicacionDTO> listarParaMapa(Integer material, String distrito, String q,
                                               BigDecimal minKg, LocalDate fecha, String estado);         // END-16
    public List<MiPublicacionDTO> listarMisPublicaciones(String email, String estado);                    // END-17
    public PublicacionDTO editar(String email, Long id, EditarPublicacionDTO editarPublicacionDTO);       // END-18
    public PublicacionDTO cancelar(String email, Long id);                                                // END-19
    public FotoPublicacionDTO subirFoto(String email, Long id, MultipartFile archivo);                    // END-20

    // Usado por SolicitudRecoleccionService (404 si no existe)
    public PublicacionMaterial obtenerPublicacion(Long id);
}
