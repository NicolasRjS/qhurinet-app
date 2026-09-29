package com.upc.qhurinet.serviceimpl;

import com.upc.qhurinet.dtos.CategoriaMaterialDTO;
import com.upc.qhurinet.dtos.ClasificacionMaterialDTO;
import com.upc.qhurinet.entities.CategoriaMaterial;
import com.upc.qhurinet.repositories.CategoriaMaterialRepositorio;
import com.upc.qhurinet.services.CategoriaMaterialService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoriaMaterialServiceImpl implements CategoriaMaterialService {
    @Autowired
    private CategoriaMaterialRepositorio categoriaMaterialRepositorio;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CategoriaMaterialDTO> listar() {
        return categoriaMaterialRepositorio.findAll()
                .stream()
                .sorted(Comparator.comparing(CategoriaMaterial::getNombre))
                .map(categoria -> modelMapper.map(categoria, CategoriaMaterialDTO.class))
                .toList();
    }

    @Override
    public ClasificacionMaterialDTO clasificar(MultipartFile foto, String descripcion) {
        boolean sinFoto = foto == null || foto.isEmpty();
        if (sinFoto && (descripcion == null || descripcion.isBlank())) {
            throw new IllegalArgumentException("foto o descripcion: se debe enviar al menos uno");
        }
        // PENDIENTE (servicio externo): la clasificacion la resuelve un servicio de IA que aun no esta definido.
        // Luego se mapea el resultado al catalogo por nombre (query) y se descarta si no existe (US 03-EP1).
        throw new UnsupportedOperationException("END-21 pendiente: falta definir el servicio de clasificación");
    }

    @Override
    public CategoriaMaterial obtenerCategoria(Integer id) {
        return categoriaMaterialRepositorio.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoría de material no encontrada"));
    }
}
