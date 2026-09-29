package com.upc.qhurinet.services;

import com.upc.qhurinet.dtos.CategoriaMaterialDTO;
import com.upc.qhurinet.dtos.ClasificacionMaterialDTO;
import com.upc.qhurinet.entities.CategoriaMaterial;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CategoriaMaterialService {
    public List<CategoriaMaterialDTO> listar();                                            // END-13
    public ClasificacionMaterialDTO clasificar(MultipartFile foto, String descripcion);    // END-21

    // Usado por PublicacionMaterialService (404 "Categoría de material no encontrada")
    public CategoriaMaterial obtenerCategoria(Integer id);
}
